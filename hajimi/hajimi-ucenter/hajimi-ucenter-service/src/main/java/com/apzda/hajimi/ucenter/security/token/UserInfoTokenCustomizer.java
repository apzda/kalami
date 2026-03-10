/*
 * Copyright 2025-2026 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.apzda.hajimi.ucenter.security.token;

import com.apzda.hajimi.configure.autoconfig.LabelConfigProperties;
import com.apzda.hajimi.configure.service.SettingService;
import com.apzda.hajimi.configure.setting.LabelSetting;
import com.apzda.hajimi.ucenter.config.UCenterServiceConfiguration;
import com.apzda.hajimi.ucenter.domain.entity.OauthSessionEntity;
import com.apzda.hajimi.ucenter.domain.entity.RbacPrivilegeEntity;
import com.apzda.hajimi.ucenter.domain.entity.RoleEntity;
import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.hajimi.ucenter.domain.repository.AgentRepository;
import com.apzda.hajimi.ucenter.domain.repository.OrganizationRepository;
import com.apzda.hajimi.ucenter.domain.repository.TenantRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.domain.vo.OrganizationVo;
import com.apzda.hajimi.ucenter.domain.vo.TenantUserVo;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.apzda.kalami.security.authentication.AuthenticationDetails;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.token.JwtToken;
import com.apzda.kalami.security.token.JwtTokenCustomizer;
import com.apzda.kalami.security.user.MetaUserDetailsService;
import com.apzda.kalami.tenant.Organization;
import com.apzda.kalami.tenant.Tenant;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.apzda.kalami.security.mfa.MfaStatus.MFA_PENDING;
import static com.apzda.kalami.security.mfa.MfaStatus.MFA_UNSET;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserInfoTokenCustomizer implements JwtTokenCustomizer {

    private final UCenterServiceConfiguration ucenterServiceConfiguration;

    private final MetaUserDetailsService metaUserDetailsService;

    private final UserRepository userRepository;

    private final TenantRepository tenantRepository;

    private final OrganizationRepository organizationRepository;

    private final AgentRepository agentRepository;

    private final SecurityConfigProperties securityConfigProperties;

    private final SettingService settingService;

    private final LabelConfigProperties labelConfigProperties;

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @Override
    @Nonnull
    public JwtToken customize(@Nonnull Authentication authentication, @Nonnull JwtToken token) {
        // log.debug("Customizing user info token");
        val userInfo = new UserToken();
        BeanUtils.copyProperties(token, userInfo);

        if (userInfo.getExtra() == null) {
            userInfo.setExtra(new HashMap<>());
        }

        try {
            val uid = Long.parseLong(userInfo.getUid());
            val user = userRepository.findByUserId(uid);
            if (user == null) {
                return userInfo;
            }
            userInfo.setUsername(user.getUsername());
            userInfo.setDisplayName(user.getNickname());
            userInfo.setAvatar(user.getAvatar());
            userInfo.setPhone(user.getPhone());
            userInfo.setEmail(user.getEmail());
            userInfo.setLandingUrl(user.getLanding());

            if (authentication.getDetails() instanceof AuthenticationDetails details) {
                fillOauthMeta(userInfo, details);
                val app = details.getApp();

                boolean mfaEnabled = false;
                if (app != null) {
                    val cfg = securityConfigProperties.getApp().get(app);
                    if (cfg != null) {
                        mfaEnabled = cfg.isMfaEnabled();
                    }
                }

                if (StringUtils.isBlank(userInfo.getRunas())) {
                    // MFA
                    fillMfa(userInfo);
                    if (userInfo.getMfa() == null && mfaEnabled) {
                        userInfo.setMfa(MFA_UNSET);
                    }
                }

            }

            // 角色与权限
            loadAuthorities(userInfo);
            // 代理商信息
            fillAgent(userInfo);
        }
        catch (Exception e) {
            log.warn("Can't fill authorities: {} - {}", userInfo.getUid(), e.getMessage());
        }
        return userInfo;
    }

    @Override
    @Nonnull
    public JwtToken refresh(@Nonnull Authentication authentication, @Nonnull JwtToken token) {
        val userInfo = new UserToken();
        BeanUtils.copyProperties(token, userInfo);

        if (userInfo.getExtra() == null) {
            userInfo.setExtra(new HashMap<>());
        }

        try {
            val uid = Long.parseLong(userInfo.getUid());
            val user = userRepository.findByUserId(uid);
            if (user == null) {
                return userInfo;
            }
            userInfo.setDisplayName(user.getNickname());
            userInfo.setAvatar(user.getAvatar());
            userInfo.setPhone(user.getPhone());
            userInfo.setEmail(user.getEmail());
            userInfo.setLandingUrl(user.getLanding());

            loadAuthorities(userInfo);
            // 代理商信息
            fillAgent(userInfo);
        }
        catch (Exception e) {
            log.warn("Can't fill authorities while refreshing: {} - {}", userInfo.getUid(), e.getMessage());
        }
        return userInfo;
    }

    public void loadAuthorities(@Nonnull UserToken token) {
        String tenantId = fillTenants(token);
        String orgId = fillOrganizations(token, tenantId);
        // 清空用户的权限缓存
        try {
            metaUserDetailsService.clearCache(tenantId, orgId, token.getUid());
        }
        catch (Exception e) {
            log.warn("Clear User Details Cache Error: {} - {}", token.getUid(), e.getMessage());
        }

        List<String> permissions = new ArrayList<>();
        if (!"0".equals(tenantId)) {
            permissions.add("TENANT");
        }

        List<RoleEntity> roles = userRepository.listUserRoles(token.getUid(), tenantId, orgId, true);
        if (CollectionUtils.isEmpty(roles)) {
            token.setAuthorities(permissions);
        }
        else {
            List<UserToken.Role> roleList = new ArrayList<>();
            val roleIds = new ArrayList<Serializable>();
            for (RoleEntity role : roles) {
                roleList.add(new UserToken.Role(role.getId(), role.getName(), role.getRole()));
                roleIds.add(role.getId());
                if (role.getLanding() != null && token.getLandingUrl() == null) {
                    token.setLandingUrl(role.getLanding());
                }
            }

            token.setRoles(roleList);

            List<RbacPrivilegeEntity> authorities = userRepository.listPrivilegesByRoleIds(roleIds, tenantId);
            if (CollectionUtils.isEmpty(authorities)) {
                token.setAuthorities(permissions);
            }
            else {
                permissions.addAll(authorities.stream().map(RbacPrivilegeEntity::getPermission).toList());
                token.setAuthorities(permissions);
            }
        }
    }

    private void fillOauthMeta(@Nonnull UserToken token, @Nonnull AuthenticationDetails details) {
        val meta = details.getMeta();
        if (meta == null) {
            return;
        }
        val provider = meta.getOrDefault("provider", UserToken.DEFAULT_PROVIDER);
        val appId = meta.getOrDefault("appId", UserToken.DEFAULT_APPID);
        OauthSessionEntity oauthSession = userRepository.getLastLoginSession(token.getUid(), provider, appId);
        if (oauthSession != null) {
            token.setLastLoginIp(oauthSession.getIp());
            token.setLastLoginTime(oauthSession.getLoginTime());
        }
    }

    private void fillMfa(@Nonnull UserToken token) {
        val authenticator = ucenterServiceConfiguration.getAuthenticator();
        val config = userRepository.findMfaByUidAndType(token.getUid(), authenticator.get().getType());
        config.ifPresent((c) -> {
            if (Boolean.TRUE.equals(c.getEnabled())) {
                token.setMfa(MFA_PENDING);
            }
        });
    }

    private String fillTenants(@Nonnull UserToken token) {
        List<TenantUserVo> tenants = tenantRepository.listTenantByUid(token.getUid());
        if (!CollectionUtils.isEmpty(tenants)) {
            val tenantList = tenants.stream().map(t -> {
                val to = new Tenant();
                to.setId(String.valueOf(t.getTenantId()));
                to.setName(t.getName());
                to.setShortName(t.getShortName());
                to.setLogo(t.getLogo());
                if (Boolean.TRUE.equals(t.getCurrent())) {
                    to.setCurrent(true);
                }
                to.setStatus(t.getStatus().name());
                return to;
            }).toList();

            val tenant = tenantList.stream()
                .filter(tenantUserVo -> Boolean.TRUE.equals(tenantUserVo.getCurrent()))
                .findFirst()
                .orElse(tenantList.get(0));

            token.setTenant(tenant);
            token.setTenants(tenantList);

            if (TenantStatus.DISABLED.name().equals(tenant.getStatus())) {
                token.setDisabled(true);
            }

            val tenantId = tenant.getId();
            token.getExtra().put("i18n", i18n(tenantId));

            return tenant.getId();
        }

        return "0";
    }

    @Nullable
    private String fillOrganizations(@Nonnull UserToken token, String tenantId) {
        List<OrganizationVo> organizations = organizationRepository.listOrganizationByTidAndUid(tenantId,
                token.getUid());
        if (!CollectionUtils.isEmpty(organizations)) {
            val vos = organizations.stream().map(t -> {
                val to = new Organization();
                to.setId(String.valueOf(t.getOrgId()));
                to.setPid(String.valueOf(t.getParentId()));
                to.setTenantId(t.getTenantId());
                to.setName(t.getName());
                to.setCode(t.getCode());
                to.setShortName(t.getShortName());
                to.setLogo(t.getLogo());
                if (Boolean.TRUE.equals(t.getCurrent())) {
                    to.setCurrent(true);
                }
                return to;
            }).toList();

            val organization = vos.stream()
                .filter(tenantUserVo -> Boolean.TRUE.equals(tenantUserVo.getCurrent()))
                .findFirst()
                .orElse(vos.get(0));

            token.setOrg(organization);
            token.setOrganizations(vos);

            return organization.getId();
        }

        return null;
    }

    private void fillAgent(@Nonnull UserToken token) {
        val agent = agentRepository.getByUid(token.getUid());

        if (agent != null) {
            token.getExtra().put("agentId", agent.getId());
            token.getExtra().put("agent", agent);
        }
    }

    @Nonnull
    private Map<String, Map<String, String>> i18n(String tenantId) {
        val setting = settingService.load(LabelSetting.class, tenantId);

        val translated = setting.getConfig().getLabels();
        val labels = labelConfigProperties.getLabels();

        labelConfigProperties.getLanguages().keySet().forEach(language -> {
            if (!translated.containsKey(language)) {
                translated.put(language, labels);
            }
            else {
                val texts = translated.get(language);
                labels.forEach((label, text) -> {
                    if (!texts.containsKey(label)) {
                        texts.put(label, text);
                    }
                });
            }
        });

        return translated;
    }

}

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

package com.apzda.hajimi.ucenter.controller.internal;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.apzda.hajimi.ucenter.config.UCenterSecurityConfigureProperties;
import com.apzda.hajimi.ucenter.config.UCenterTenantConfigureProperties;
import com.apzda.hajimi.ucenter.controller.admin.dto.AgentPageQuery;
import com.apzda.hajimi.ucenter.controller.admin.dto.TenantPageQuery;
import com.apzda.hajimi.ucenter.data.UserAuthoritiesCachedData;
import com.apzda.hajimi.ucenter.domain.entity.*;
import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.hajimi.ucenter.domain.enums.UserInfoScope;
import com.apzda.hajimi.ucenter.domain.repository.*;
import com.apzda.hajimi.ucenter.domain.vo.OAuthVo;
import com.apzda.hajimi.ucenter.domain.vo.OrganizationVo;
import com.apzda.hajimi.ucenter.domain.vo.RoleVo;
import com.apzda.hajimi.ucenter.domain.vo.UserVo;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.RolePrivilegeMapper;
import com.apzda.hajimi.ucenter.oauth.OauthResolver;
import com.apzda.hajimi.ucenter.security.AuthorityLoader;
import com.apzda.hajimi.ucenter.service.UCenterService;
import com.apzda.hajimi.ucenter.service.dto.*;
import com.apzda.hajimi.ucenter.tenant.DefaultSubscription;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.apzda.kalami.agent.Agent;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.service.ICommonService;
import com.apzda.kalami.service.TempStorageService;
import com.apzda.kalami.tenant.Organization;
import com.apzda.kalami.tenant.Tenant;
import com.google.common.base.Splitter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Primary;
import org.springframework.data.util.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Primary
public class UCenterServiceController implements UCenterService, ApplicationContextAware {

    private final ObjectProvider<AuthorityLoader> loaderProvider;

    private final ObjectProvider<OauthResolver> appIdResolverProvider;

    private final TempStorageService tempStorageService;

    private final UCenterConverter userConverter;

    private final UserRepository userRepository;

    private final ResourceRepository resourceRepository;

    private final RoleRepository roleRepository;

    private final PrivilegeRepository privilegeRepository;

    private final TenantRepository tenantRepository;

    private final OrganizationRepository organizationRepository;

    private final RolePrivilegeMapper rolePrivilegeMapper;

    private final UCenterTenantConfigureProperties properties;

    private final OauthRepository oauthRepository;

    private final AgentRepository agentRepository;

    private final ICommonService commonUtils;

    private final ApplicationContextAware configurationPropertiesRebinder;

    @Value("${kalami.security.role-prefix:ROLE_}")
    private String rolePrefix = "ROLE_";

    private Lazy<CacheManager> cacheManagerLoader;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.cacheManagerLoader = Lazy.of(() -> {
            try {
                return applicationContext.getBean(CacheManager.class);
            }
            catch (Exception e) {
                return null;
            }
        });
    }

    @Override
    @Nullable
    public Optional<UserVo> getUser(Long id) {
        val user = userRepository.findByUserId(id);
        if (user == null) {
            return Optional.empty();
        }
        val vo = userConverter.from(user);
        return Optional.of(vo);
    }

    @Override
    public Optional<UserVo> getTenantUserInfo(String tenantId, Long uid, List<UserInfoScope> scops) {
        val tu = userRepository.getTenantUserByUid(tenantId, uid);
        if (tu == null) {
            return Optional.empty();
        }
        val user = userRepository.findByUserId(uid);
        if (user == null) {
            return Optional.empty();
        }
        val vo = userConverter.from(user);
        vo.setTenantId(tenantId);
        fillUserInfoByScopes(vo, scops);
        return Optional.of(vo);
    }

    @Override
    public Optional<UserVo> getOrganizationUserInfo(String tenantId, String orgId, Long uid,
            List<UserInfoScope> scops) {
        val ou = userRepository.getOrganizationUserByUid(tenantId, orgId, uid);
        if (ou == null) {
            return Optional.empty();
        }
        val user = userRepository.findByUserId(uid);
        if (user == null) {
            return Optional.empty();
        }
        val vo = userConverter.from(user);
        vo.setTenantId(tenantId);
        vo.setOrgId(orgId);

        fillUserInfoByScopes(vo, scops);
        return Optional.of(vo);

    }

    @Override
    @Nonnull
    public UserVo createUser(
            @RequestBody @Validated({ Group.New.class, UCenterService.class, Group.Default.class }) UserDto user) {
        val entity = userRepository.create(user);
        return userConverter.from(entity);
    }

    @Override
    @Nonnull
    public UserVo updateUser(
            @RequestBody @Validated({ Group.Update.class, UCenterService.class, Group.Default.class }) UserDto user) {
        val entity = userRepository.update(user);

        return userConverter.from(entity);
    }

    @Override
    public Optional<OAuthVo> getOauthByProviderAndAppAndUid(String provider, String appId, String uid) {
        String resolvedId = appId;
        OauthResolver oauthResolver = null;
        for (OauthResolver resolver : appIdResolverProvider.orderedStream().toList()) {
            if (resolver.supports(provider)) {
                resolvedId = resolver.resolveAppId(appId);
                if (resolvedId != null) {
                    oauthResolver = resolver;
                    break;
                }
            }
        }

        if (resolvedId == null) {
            log.warn("OAuth provider not found for appId {}", appId);
            return Optional.empty();
        }

        OauthEntity oAuth = oauthRepository.getByProviderAndAppIdAndUid(provider, resolvedId, uid);

        if (oAuth == null) {
            return Optional.empty();
        }

        if (oauthResolver != null) {
            oAuth.setOpenId(oauthResolver.resolveOpenId(oAuth.getOpenId()));
            oAuth.setUnionId(oauthResolver.resolveUnionId(oAuth.getUnionId()));
        }

        return Optional.of(userConverter.from(oAuth));
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthority(String id) {
        val cKey = "grant." + MD5.create().digestHex(id);
        try {
            val cached = tempStorageService.load(cKey, UserAuthoritiesCachedData.class);
            if (cached.isPresent()) {
                return cached.get().getAuthorities();
            }
        }
        catch (Exception e) {
            log.warn("Cannot get user({})'s authorities from cache: {}", id, e.getMessage());
        }
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        try {
            val key = Splitter.on("@").omitEmptyStrings().trimResults().splitToList(id);
            val tenantId = key.get(1);
            val orgId = key.size() == 3 ? key.get(2) : null;
            val uid = key.get(0);
            // 加载三方
            loaderProvider.orderedStream().forEach((loader) -> {
                try {
                    val perms = loader.load(tenantId, orgId, uid);
                    if (!CollectionUtils.isEmpty(perms)) {
                        authorities.addAll(perms);
                    }
                }
                catch (Exception e) {
                    log.warn("Cannot load authority from loader: {} - {}", loader.getClass(), e.getMessage());
                }
            });

            val roles = userRepository.listUserRoles(uid, tenantId, orgId, true);
            if (roles.isEmpty()) {
                return authorities.stream().toList();
            }
            val permissions = new HashSet<String>();
            val ids = roles.stream().peek(role -> {
                val rp = rolePrefix + role.getRole();
                if (permissions.add(rp)) {
                    authorities.add(new SimpleGrantedAuthority(rp));
                }
            }).map((role) -> (Serializable) role.getId()).toList();

            userRepository.listPrivilegesByRoleIds(ids, tenantId).forEach(privilege -> {
                if (permissions.add(privilege.getPermission())) {
                    authorities.add(new SimpleGrantedAuthority(privilege.getPermission()));
                }
            });

            if (!"0".equals(tenantId) && permissions.add("TENANT")) {
                authorities.add(new SimpleGrantedAuthority("TENANT"));
            }

            return authorities.stream().toList();
        }
        finally {
            val cachedData = new UserAuthoritiesCachedData();
            cachedData.setAuthorities(authorities.stream().toList());
            try {
                tempStorageService.save(cKey, cachedData);
            }
            catch (Exception e) {
                log.warn("Cannot save user({})'s authorities to cache: {}", id, e.getMessage());
            }
        }
    }

    @Override
    public List<DefaultSubscription> getSubscriptions(String tenantId) {
        return tenantRepository.listSubscriptionsById(tenantId);
    }

    @Override
    public DefaultSubscription subscribe(SubscribeDto dto) {
        return tenantRepository.subscribe(dto);
    }

    @Override
    public Response<Boolean> sync(@Validated UCenterSecurityConfigureProperties properties) {
        try {
            syncResources(properties.getResources());
        }
        catch (Exception e) {
            log.warn("sync resources failed: {}", e.getMessage());
        }

        try {
            val res = new UCenterSecurityConfigureProperties.Resource();
            syncPrivileges(properties.getPrivileges(), res);
        }
        catch (Exception e) {
            log.warn("sync privileges failed: {}", e.getMessage());
        }

        try {
            syncRoles(properties.getRoles());
        }
        catch (Exception e) {
            log.warn("sync roles failed: {}", e.getMessage());
        }
        return Response.success(true);
    }

    @Override
    @Nullable
    public Optional<Tenant> getTenant(String id) {
        val tenant = tenantRepository.getById(id);
        if (tenant == null) {
            return Optional.empty();
        }

        val t = tenant.toTenant();
        t.setChildren(tenantRepository.listChildren(id));
        return Optional.of(t);
    }

    @Override
    public Optional<Agent> getAgent(String id) {
        val agent = agentRepository.getById(id);
        if (agent == null) {
            return Optional.empty();
        }
        val a = agent.toAgent();
        a.setChildren(agentRepository.listChildren(id));

        return Optional.of(a);
    }

    @Override
    public Optional<Agent> getAgentByUid(String uid) {
        AgentEntity agent = agentRepository.getByUid(uid);
        if (agent == null) {
            return Optional.empty();
        }

        val a = agent.toAgent();
        a.setChildren(agentRepository.listChildren(a.getId()));

        return Optional.of(a);
    }

    @Override
    public List<Agent> getAgents(AgentQuery query) {
        val page = new AgentPageQuery();
        BeanUtils.copyProperties(query, page);
        val results = agentRepository.pageQuery(page, new QuickSearch());
        if (CollectionUtils.isEmpty(results.getRecords())) {
            return Collections.emptyList();
        }

        return userConverter.toAgent(results.getRecords());
    }

    @Override
    public Paged<Agent> agents(AgentQuery query) {
        val page = new AgentPageQuery();
        BeanUtils.copyProperties(query, page);
        val results = agentRepository.pageQuery(page, new QuickSearch());

        return PageUtil.from(results, userConverter::toAgent);
    }

    @Override
    public Response<Boolean> createAgent(@Validated({ Group.Default.class, Group.New.class }) AgentDto agentDto) {
        agentRepository.create(agentDto);
        return Response.success(true);
    }

    @Override
    public Response<Boolean> updateAgent(@Validated({ Group.Default.class, Group.Update.class }) AgentDto agentDto) {
        agentRepository.update(agentDto);
        return Response.success(true);
    }

    @Override
    public List<Tenant> getTenants(String uid) {
        val tenants = tenantRepository.listTenantByUid(uid);

        if (CollectionUtils.isEmpty(tenants)) {
            return List.of();
        }

        return tenants.stream().map(vo -> {
            val tenant = new Tenant();
            tenant.setId(String.valueOf(vo.getTenantId()));
            tenant.setPid(String.valueOf(vo.getParentId()));
            tenant.setAgentId(String.valueOf(vo.getAgentId()));
            tenant.setName(StringUtils.defaultIfBlank(vo.getName(), properties.getName()));
            tenant.setLogo(StringUtils.defaultIfBlank(vo.getLogo(), properties.getLogo()));
            if (vo.getTenantId() == 0) {
                tenant.setShortName(StringUtils.defaultIfBlank(vo.getShortName(), properties.getShortName()));
            }
            else {
                tenant.setShortName(vo.getShortName());
            }
            tenant.setPhone(vo.getPhone());
            tenant.setContact(vo.getContact());
            tenant.setAddress(vo.getAddress());
            tenant.setStatus(vo.getStatus().name());
            if (Boolean.TRUE.equals(vo.getCurrent())) {
                tenant.setCurrent(true);
            }
            tenant.setChildren(tenantRepository.listChildren(tenant.getId()));

            return tenant;
        }).toList();
    }

    @Override
    public Paged<Tenant> tenants(@Nonnull @Validated TenantRequest request) {
        if (request.getParentId() == null) {
            request.setParentId(0L);
        }
        val qs = new QuickSearch();
        val query = new TenantPageQuery(request);

        if (StringUtils.isNotBlank(query.getName())) {
            qs.setKeyword(query.getName());
            qs.setFields(List.of("name"));
            query.setName(null);
        }

        val result = tenantRepository.pageQuery(query, qs);

        return PageUtil.from(result, userConverter::toTenant);
    }

    @Override
    public List<RoleVo> listUserRolesByUsername(String tenantId, String username) {
        val user = userRepository.findByUsername(username);
        if (user == null) {
            return List.of();
        }

        return userConverter.fromRoles(userRepository.listUserRoles(user.getId(), tenantId));
    }

    @Override
    public Tenant createTenant(@RequestBody @Validated({ Group.New.class, Group.Default.class }) TenantDto dto) {
        val tenant = tenantRepository.create(dto);

        return tenant.toTenant();
    }

    @Override
    @Transactional
    public Tenant createTenant(@RequestBody @Validated @Nonnull SimpleTenantDto dto) {
        val t = new TenantDto();

        t.setCode(commonUtils.sn("666", "TENANT.SN"));
        t.setTenantId(dto.getTenantId());
        t.setAgentId(dto.getAgentId());
        t.setName(dto.getName());
        t.setShortName(dto.getShortName());
        t.setContact(dto.getContact());
        t.setAddress(dto.getAddress());
        t.setLogo(dto.getLogo());
        t.setStatus(TenantStatus.ENABLED);
        t.setPhone(dto.getPhone());
        t.setRemark(dto.getRemark());
        t.setUsername(dto.getPhone());
        t.setPassword(RandomUtil.randomString(12));
        t.setConfirmPassword(t.getPassword());
        val tenant = createTenant(t);

        val services = dto.getServices();
        if (!CollectionUtils.isEmpty(services)) {
            services.forEach(service -> {
                val subscribe = new SubscribeDto();
                subscribe.setTenantId(tenant.getId());
                subscribe.setService(service.getId());
                subscribe.setExpireTime(service.getExpireTime());
                subscribe.setExpired(0);

                this.tenantRepository.subscribe(subscribe);
            });
        }

        return tenant;
    }

    @Override
    public Tenant updateTenant(@RequestBody @Validated({ Group.Update.class, Group.Default.class }) TenantDto dto) {
        val tenant = tenantRepository.update(dto);

        return tenant.toTenant();
    }

    @Override
    @Nullable
    public Optional<Organization> getOrganization(String orgId) {
        val organization = organizationRepository.getById(orgId);
        if (organization == null) {
            return Optional.empty();
        }

        val org = new Organization();
        org.setId(orgId);
        org.setPid(String.valueOf(organization.getParentId()));
        org.setTenantId(organization.getTenantId());
        org.setName(organization.getName());
        org.setShortName(organization.getShortName());
        org.setDescription(organization.getRemark());
        org.setCode(organization.getCode());
        org.setLogo(organization.getLogo());
        org.setChildren(organizationRepository.listChildren(organization.getTenantId(), organization.getId()));
        return Optional.of(org);
    }

    @Override
    public List<Organization> getOrganizations(String tenantId, String uid) {
        List<OrganizationVo> organizations = organizationRepository.listOrganizationByTidAndUid(tenantId, uid);

        if (CollectionUtils.isEmpty(organizations)) {
            return List.of();
        }

        return organizations.stream().map(org -> {
            val organization = new Organization();
            organization.setId(String.valueOf(org.getOrgId()));
            organization.setPid(String.valueOf(org.getParentId()));
            organization.setTenantId(org.getTenantId());
            organization.setName(org.getName());
            organization.setCode(org.getCode());
            organization.setLogo(org.getLogo());
            organization.setUid(uid);
            organization.setCurrent(false);
            organization.setChildren(organizationRepository.listChildren(org.getTenantId(), org.getId()));
            return organization;
        }).toList();
    }

    @Override
    @Transactional
    public Organization createOrganization(
            @RequestBody @Validated({ Group.New.class, Group.Default.class }) OrganizationDto dto) {
        OrganizationEntity entity = organizationRepository.create(dto);

        val org = new Organization();
        org.setId(String.valueOf(entity.getId()));
        org.setPid(String.valueOf(entity.getParentId()));
        org.setTenantId(entity.getTenantId());
        org.setCode(entity.getCode());
        org.setName(entity.getName());
        org.setShortName(entity.getShortName());
        org.setDescription(entity.getRemark());
        org.setLogo(entity.getLogo());

        return org;
    }

    @Override
    @Transactional
    public Organization updateOrganization(
            @RequestBody @Validated({ Group.Update.class, Group.Default.class }) OrganizationDto dto) {
        OrganizationEntity entity = organizationRepository.update(dto);

        val org = new Organization();
        org.setId(String.valueOf(entity.getId()));
        org.setPid(String.valueOf(entity.getParentId()));
        org.setTenantId(entity.getTenantId());
        org.setCode(entity.getCode());
        org.setName(entity.getName());
        org.setShortName(entity.getShortName());
        org.setDescription(entity.getRemark());
        org.setLogo(entity.getLogo());

        return org;
    }

    @Override
    public Response<Boolean> joinOrganization(Long tenantId, Long orgId, Long uid) {
        val ou = organizationRepository.join(tenantId, orgId, uid);
        return Response.success(true);
    }

    @Override
    public Response<Boolean> leaveOrganization(Long tenantId, Long orgId, Long uid) {
        return Response.success(organizationRepository.leave(tenantId, orgId, uid));
    }

    @Override
    public Response<Boolean> kickoff(String id) {
        userRepository.kickoff(id);
        return Response.success(true);
    }

    @Override
    public Response<Boolean> clearUserCache(String tenantId, String orgId, String username) {
        val tid = Optional.ofNullable(tenantId).orElse("0");
        val oid = Optional.ofNullable(orgId).orElse("");
        val id = String.format("%s@%s@%s", username, tid, oid);
        val key = "grant." + MD5.create().digestHex(id);

        try {
            tempStorageService.remove(key);
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to clear authorities for {} - {}", id, e.getMessage());
            }
        }

        cacheManagerLoader.getOptional().ifPresent(cacheManager -> {
            // 清空用户所属租户列表缓存
            val ucache = cacheManager.getCache("uc:tn:users");
            if (ucache != null) {
                ucache.evict(username);
            }
            // 清空用户所属部门(组织)缓存
            val scache = cacheManager.getCache("uc:org:users");
            if (scache != null) {
                scache.evict(tid + "." + username);
            }
        });

        return Response.success(true);
    }

    private void fillUserInfoByScopes(@Nonnull UserVo userVo, List<UserInfoScope> scopes) {
        val uid = userVo.getId();
        val tenantId = userVo.getTenantId();
        val orgId = userVo.getOrgId();
        if (!CollectionUtils.isEmpty(scopes)) {
            scopes.forEach(scope -> {
                switch (scope) {
                    case SECURITY -> {
                        val roles = userRepository.listUserRoles(uid, tenantId, orgId, true);
                        if (!CollectionUtils.isEmpty(roles)) {
                            userVo.setRoles(userConverter.fromRoles(roles));
                            val privileges = userRepository.listPrivilegesByRoleIds(
                                    roles.stream().map((r) -> (Serializable) r.getId()).toList(), tenantId);
                            if (!CollectionUtils.isEmpty(privileges)) {
                                userVo.setPrivileges(userConverter.fromPrivileges(privileges));
                            }
                        }
                    }
                    case META -> {

                    }
                }
            });
        }
    }

    /**
     * 同步一级资源
     * @param resources 一级资源列表.
     */
    private void syncResources(Map<String, UCenterSecurityConfigureProperties.Resource> resources) {
        if (!CollectionUtils.isEmpty(resources)) {
            val parent = new RbacResourceEntity();
            parent.setId(0L);
            for (Map.Entry<String, UCenterSecurityConfigureProperties.Resource> resource : resources.entrySet()) {
                val rid = resource.getKey();
                val res = resource.getValue();
                syncResource(rid, res, parent);
                // 同步一级资源定义的权限
                val privileges = res.getPrivileges();
                syncPrivileges(privileges, res);
            }
        }
    }

    /**
     * 同步资源.
     * @param resources 资源列表
     * @param parent 上级资源
     */
    private void syncResources(Map<String, UCenterSecurityConfigureProperties.Resource> resources,
            RbacResourceEntity parent) {
        if (!CollectionUtils.isEmpty(resources)) {
            for (Map.Entry<String, UCenterSecurityConfigureProperties.Resource> resource : resources.entrySet()) {
                val rid = resource.getKey();
                val res = resource.getValue();
                if (StringUtils.isBlank(res.getService())) {
                    res.setService(parent.getService());
                }
                syncResource(rid, res, parent);
                // 同步一级资源定义的权限
                val privileges = res.getPrivileges();
                syncPrivileges(privileges, res);
            }
        }
    }

    /**
     * 同步资源.
     * @param rid 资源ID
     * @param res 资源
     * @param parent 父资源
     */
    private void syncResource(String rid, @Nonnull UCenterSecurityConfigureProperties.Resource res,
            @Nonnull RbacResourceEntity parent) {
        RbacResourceEntity resource = resourceRepository.findByPidAndRid(parent.getId(), rid);
        val actions = res.getActions();
        val explorer = res.getExplorer();

        if (resource == null) {
            resource = new RbacResourceEntity();
            resource.setBuiltin(true);
            resource.setPid(parent.getId());
            resource.setRid(rid);
        }
        resource.setName(res.getName());
        resource.setService(res.getService());
        if (CollectionUtils.isEmpty(actions)) {
            resource.setActions("c,r,u,d");
        }
        else {
            resource.setActions(String.join(",", actions));
        }

        if (explorer != null) {
            resource.setExplorer(explorer.getCanonicalName());
        }
        else {
            resource.setExplorer(null);
        }
        resource.setDescription(res.getDescription());
        if (resource.getId() == null) {
            resourceRepository.save(resource);
        }
        else {
            resourceRepository.updateById(resource);
        }

        if (!CollectionUtils.isEmpty(res.getChildren())) {
            syncResources(res.getChildren(), resource);
        }
    }

    /**
     * 同步角色列表.
     * @param roles 角色列表.
     */
    private void syncRoles(Map<String, UCenterSecurityConfigureProperties.Role> roles) {
        if (!CollectionUtils.isEmpty(roles)) {
            for (Map.Entry<String, UCenterSecurityConfigureProperties.Role> entry : roles.entrySet()) {
                val rid = entry.getKey();
                val role = entry.getValue();
                syncRole(rid, role);
            }
        }
    }

    /**
     * 同步角色
     * @param rid 角色ID
     * @param role 角色
     */
    private void syncRole(String rid, @Nonnull UCenterSecurityConfigureProperties.Role role) {
        RoleEntity entity = roleRepository.findByRoleAndTenantId(rid, "0");
        if (entity == null) {
            entity = new RoleEntity();
            entity.setTenantId("0");
            entity.setRole(rid);
            entity.setProvider(UserToken.DEFAULT_PROVIDER);
        }
        entity.setBuiltin(true);
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setLeasable(role.isLeasable());

        if (entity.getId() == null) {
            roleRepository.save(entity);
        }
        else {
            roleRepository.updateById(entity);
        }
        val privileges = role.getPrivileges();

        if (!CollectionUtils.isEmpty(privileges)) {
            val roleId = entity.getId();
            List<RolePrivilegeEntity> ps = new ArrayList<>();
            privileges.forEach(privilege -> {
                val pEntity = privilegeRepository.findByPermissionAndTenantId(privilege, "0");
                if (pEntity == null) {
                    return;
                }

                if (!rolePrivilegeMapper.existsByRoleIdAndPrivilegeId(roleId, pEntity.getId())) {
                    RolePrivilegeEntity rolePrivilegeEntity = new RolePrivilegeEntity();
                    rolePrivilegeEntity.setTenantId("0");
                    rolePrivilegeEntity.setRoleId(roleId);
                    rolePrivilegeEntity.setPrivilegeId(pEntity.getId());
                    ps.add(rolePrivilegeEntity);
                }
            });

            if (!CollectionUtils.isEmpty(ps)) {
                rolePrivilegeMapper.insert(ps);
            }
        }
    }

    /**
     * 同步权限列表.
     * @param privileges 权限列表.
     * @param res 资源
     */
    private void syncPrivileges(Map<String, UCenterSecurityConfigureProperties.Privilege> privileges,
            UCenterSecurityConfigureProperties.Resource res) {
        if (!CollectionUtils.isEmpty(privileges)) {
            for (Map.Entry<String, UCenterSecurityConfigureProperties.Privilege> entry : privileges.entrySet()) {
                val permission = entry.getKey();
                val privilege = entry.getValue();
                if (StringUtils.isBlank(privilege.getService())) {
                    privilege.setService(res.getService());
                }
                syncPrivilege(permission, privilege);
            }
        }
    }

    /**
     * 同步权限.
     * @param permission 权限
     * @param privilege 明细
     */
    private void syncPrivilege(String permission, @Nonnull UCenterSecurityConfigureProperties.Privilege privilege) {
        RbacPrivilegeEntity entity = privilegeRepository.findByPermissionAndTenantId(permission, "0");
        if (entity == null) {
            entity = new RbacPrivilegeEntity();
            entity.setTenantId("0");
            entity.setPermission(permission);
        }
        entity.setBuiltin(true);
        entity.setType(privilege.getType());
        entity.setName(privilege.getName());
        entity.setDescription(privilege.getDescription());
        entity.setExtra(privilege.getExtra());
        entity.setService(privilege.getService());

        if (entity.getId() == null) {
            privilegeRepository.save(entity);
        }
        else {
            privilegeRepository.updateById(entity);
        }

    }

}

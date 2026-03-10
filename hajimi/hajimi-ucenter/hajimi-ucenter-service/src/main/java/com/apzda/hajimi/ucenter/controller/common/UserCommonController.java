/*
 * Copyright 2026 the original author or authors.
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
package com.apzda.hajimi.ucenter.controller.common;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.apzda.hajimi.auditor.Audit;
import com.apzda.hajimi.auditor.AuditContextHolder;
import com.apzda.hajimi.ucenter.config.UCenterConfigProperties;
import com.apzda.hajimi.ucenter.config.UCenterServiceConfiguration;
import com.apzda.hajimi.ucenter.controller.common.dto.*;
import com.apzda.hajimi.ucenter.controller.common.vo.MfaConfigRes;
import com.apzda.hajimi.ucenter.domain.entity.UserAccountTransactionEntity;
import com.apzda.hajimi.ucenter.domain.entity.UserEntity;
import com.apzda.hajimi.ucenter.domain.entity.UserMfaEntity;
import com.apzda.hajimi.ucenter.domain.repository.OrganizationRepository;
import com.apzda.hajimi.ucenter.domain.repository.TenantRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.domain.vo.RoleVo;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.hajimi.ucenter.mfa.Authenticator;
import com.apzda.hajimi.ucenter.security.authentication.token.RefreshAuthenticationToken;
import com.apzda.hajimi.ucenter.security.token.UCenterTokenManager;
import com.apzda.hajimi.ucenter.service.dto.TransQueryDto;
import com.apzda.hajimi.ucenter.service.vo.AccountSummaryVo;
import com.apzda.hajimi.ucenter.service.vo.TransactionVo;
import com.apzda.hajimi.ucenter.utils.UserUtils;
import com.apzda.kalami.annotation.Dictionary;
import com.apzda.kalami.data.MapConfig;
import com.apzda.kalami.data.MessageType;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.error.ResourceNotFoundError;
import com.apzda.kalami.error.ResourceUpdateError;
import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.security.authentication.AuthenticationUtil;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.error.AuthenticationError;
import com.apzda.kalami.security.exception.TokenException;
import com.apzda.kalami.security.token.DefaultToken;
import com.apzda.kalami.security.token.JwtToken;
import com.apzda.kalami.security.token.JwtTokenCustomizer;
import com.apzda.kalami.security.token.TokenManager;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public class UserCommonController {

    protected final PasswordEncoder passwordEncoder;

    protected final TokenManager tokenManager;

    protected final UCenterServiceConfiguration configuration;

    protected final UserRepository repository;

    protected final UCenterConfigProperties properties;

    protected final TenantRepository tenantRepository;

    protected final UserAccountRepository userAccountRepository;

    protected final OrganizationRepository organizationRepository;

    protected final UCenterConverter ucenterConverter;

    protected final ObjectProvider<JwtTokenCustomizer> customizers;

    protected final Lazy<Authenticator> authenticatorLoader;

    public UserCommonController(@Nonnull ApplicationContext applicationContext) {
        this.passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
        this.tokenManager = applicationContext.getBean(TokenManager.class);
        this.repository = applicationContext.getBean(UserRepository.class);
        this.configuration = applicationContext.getBean(UCenterServiceConfiguration.class);
        this.ucenterConverter = applicationContext.getBean(UCenterConverter.class);
        this.tenantRepository = applicationContext.getBean(TenantRepository.class);
        this.organizationRepository = applicationContext.getBean(OrganizationRepository.class);
        this.userAccountRepository = applicationContext.getBean(UserAccountRepository.class);
        this.customizers = applicationContext.getBeanProvider(JwtTokenCustomizer.class);
        this.properties = this.configuration.getProperties();
        this.authenticatorLoader = this.configuration.getAuthenticator();
    }

    /**
     * 刷新AccessToken
     */
    @PostMapping("/refresh-token")
    public void refresh(@RequestBody @Validated RefreshTokenReq req, HttpServletRequest request,
            HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = tokenManager.restore(req.getAccessToken());
        }
        catch (AuthenticationError e) {
            authentication = e.getAuthentication();
        }
        if (authentication instanceof JwtTokenAuthentication jwtAuth) {
            val jwtToken = jwtAuth.getJwtToken();
            verifyToken(jwtToken);

            jwtToken.setRefreshToken(req.getRefreshToken());
            val token = new RefreshAuthenticationToken(jwtToken, request);
            // 委派给AuthenticationManager
            AuthenticationUtil.authenticate(token, request, response);
        }
        else {
            throw new AuthenticationError(ServiceError.UNAUTHORIZED);
        }
    }

    /**
     * 切换租户
     */
    @PostMapping("/switch/tenant")
    public Response<JwtToken> switchTenant(@RequestBody @Validated SwitchTenantReq req, @Nonnull CurrentUser user) {
        val entity = repository.findByUserId(user.getUid());
        if (entity == null) {
            return Response.error(new ResourceNotFoundError("用户", user.getUid()));
        }
        val id = req.getTenantId();
        val tenant = tenantRepository.getById(id);
        if (tenant == null) {
            return Response.error(new ResourceNotFoundError("租户", id));
        }

        val tUser = tenantRepository.findUserByTenantAndUid(Long.valueOf(id), entity.getId());
        if (tUser == null) {
            return Response.error("您不是该租户的用户");
        }

        if (!tenantRepository.switchTo(tenant.getId(), entity.getId())) {
            return Response.error("切换租户失败");
        }

        return Response.success(createRefreshToken(entity, req.getRefreshToken())).notify("租户切换成功");
    }

    /**
     * 切换组织部门
     */
    @PostMapping("/switch/org")
    public Response<JwtToken> switchOrganization(@RequestBody @Validated SwitchOrgReq req, @Nonnull CurrentUser user) {
        val entity = repository.findByUserId(user.getUid());
        if (entity == null) {
            return Response.error(new ResourceNotFoundError("用户", user.getUid()));
        }
        val id = req.getOrgId();
        val organization = organizationRepository.getById(id);
        if (organization == null) {
            return Response.error(new ResourceNotFoundError("部门", String.valueOf(id)));
        }

        val tUser = organizationRepository.findUserByTidAndOrgIdAndUid(req.getTenantId(), Long.valueOf(id),
                entity.getId());

        if (tUser == null) {
            return Response.error("您不是该部门的用户");
        }

        if (!organizationRepository.switchTo(organization, entity.getId())) {
            return Response.error("切换部门失败");
        }

        return Response.success(createRefreshToken(entity, req.getRefreshToken())).notify("切换成功");
    }

    /**
     * 用户角色列表
     * @param uid 用户ID
     */
    @GetMapping("/user/roles/{uid}")
    @PreAuthorize("@authz.iCan('r:core.user')")
    public Response<List<RoleVo>> userRoles(@PathVariable String uid) {
        val roles = repository.listUserRoles(uid, TenantManager.tenantId("0"), null, false);

        return Response.success(ucenterConverter.fromRoles(roles));
    }

    /**
     * 退出
     */
    @GetMapping("/logout")
    @Audit(activity = "logout", message = "logout successfully")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        AuthenticationUtil.logout(request, response);
    }

    /**
     * 修改用户资料
     */
    @PostMapping("/update-base-info")
    @Audit(activity = "修改用户资料", target = "用户", message = "成功", error = "失败")
    public Response<Boolean> changeBaseInfo(@RequestBody @Validated ModifyBaseInfoReq req, CurrentUser user) {
        AuditContextHolder.getContext().setNewValue(req);
        val entity = repository.findByUserId(user.getUid());
        if (entity == null) {
            return Response.error(new ResourceNotFoundError("用户", user.getUid()));
        }

        BeanUtil.copyProperties(req, entity, CopyOptions.create().ignoreNullValue());
        if (req.getDisplayName() != null) {
            entity.setNickname(req.getDisplayName());
        }

        if (repository.updateById(entity)) {
            return Response.success(true);
        }

        return Response.error(new ResourceUpdateError("用户", user.getUid()));
    }

    /**
     * 修改用户密码
     */
    @PostMapping("/passwd")
    @Audit(activity = "修改用户密码", target = "用户", message = "成功", error = "失败")
    @Transactional
    public Response<JwtToken> passwd(@RequestBody @Validated PasswdReq req, CurrentUser user) {
        val entity = repository.findByUserId(user.getUid());
        if (entity == null) {
            return Response.error(new ResourceNotFoundError("用户", user.getUid()));
        }
        val oldPassword = req.getOldPassword();
        if (!passwordEncoder.matches(oldPassword, entity.getPasswd())) {
            return Response.error("原密码错误");
        }
        if (oldPassword.equals(req.getNewPassword())) {
            return Response.error("新密码不能与原密码相同");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return Response.error("确认密码与新密码不一致");
        }

        if (repository.lambdaUpdate()
            .set(UserEntity::getPasswd, passwordEncoder.encode(req.getNewPassword()))
            .set(UserEntity::getPasswdExpiredAt, null)
            .eq(UserEntity::getId, user.getUid())
            .update()) {
            if (StringUtils.isNotBlank(req.getRefreshToken())) {
                entity.setPasswdExpiredAt(null);
                return Response.success(createRefreshToken(entity, req.getRefreshToken())).notify("密码重置成功");
            }
            else {
                return Response.ok("");
            }
        }

        return Response.error("修改密码失败");
    }

    /**
     * 重新加载权限
     */
    @GetMapping("/reload")
    public Response<JwtToken> reload(CurrentUser user) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val builder = DefaultToken.builder();
        builder.uid(user.getUid());

        JwtToken refreshedToken = builder.build();
        for (val customizer : customizers.orderedStream().toList()) {
            refreshedToken = customizer.refresh(authentication, refreshedToken);
        }

        return Response.success(refreshedToken);
    }

    /**
     * 获取MFA配置信息
     */
    @GetMapping("/mfa/config")
    public Response<MfaConfigRes> getMfaConfig(CurrentUser user) {
        val entity = repository.findByUserId(user.getUid());
        if (entity == null) {
            return Response.error(new ResourceNotFoundError("用户", user.getUid()));
        }

        return Response.success(setupUserMfa(entity, false));
    }

    /**
     * 验证并设置MFA
     */
    @PostMapping("/mfa/setup")
    @Audit(activity = "配置MFA", target = "用户", message = "成功", error = "失败")
    public Response<JwtToken> setupMfa(@RequestBody SetupMfaReq req, CurrentUser user) {
        val authenticator = this.authenticatorLoader.get();
        val type = authenticator.getType();
        val entity = repository.findByUserId(user.getUid());
        if (entity == null) {
            return Response.error(new ResourceNotFoundError("用户", user.getUid()));
        }
        val mfaOpt = repository.findMfaByUidAndType(user.getUid(), type);
        if (mfaOpt.isEmpty()) {
            return Response.error("MFA未配置");
        }
        val password = req.getPassword();
        if (!passwordEncoder.matches(password, entity.getPasswd())) {
            return Response.<JwtToken>error("密码不正确").withType(MessageType.NOTIFY);
        }
        val userMfa = mfaOpt.get();
        if (!authenticator.verify(req.getCode(), userMfa.getSecretKey())) {
            return Response.<JwtToken>error("验证码不正确").withType(MessageType.NOTIFY);
        }
        userMfa.setEnabled(true);
        try {
            if (!repository.updateMfa(userMfa)) {
                return Response.error(new ResourceUpdateError("MFA", user.getUid()));
            }
        }
        catch (Exception e) {
            return Response.error(new ResourceUpdateError("MFA", user.getUid()));
        }

        return Response.success(createVerifiedToken(entity, req.getRefreshToken())).notify("多重认证设置成功");
    }

    /**
     * 重置MFA
     */
    @PostMapping("/mfa/reset")
    @Audit(activity = "重置MFA", target = "用户", message = "成功", error = "失败")
    public Response<MfaConfigRes> resetMfa(CurrentUser user) {
        val entity = repository.findByUserId(user.getUid());
        if (entity == null) {
            return Response.error(new ResourceNotFoundError("用户", user.getUid()));
        }

        return Response.success(setupUserMfa(entity, true));
    }

    /**
     * 验证MFA
     */
    @PostMapping("/mfa/verify")
    public Response<JwtToken> verifyMfa(@RequestBody VerifyMfaReq req, CurrentUser user) {
        val authenticator = this.authenticatorLoader.get();
        val entity = repository.findByUserId(user.getUid());
        if (entity == null) {
            return Response.error(new ResourceNotFoundError("用户", user.getUid()));
        }
        val type = authenticator.getType();

        val mfaOpt = repository.findMfaByUidAndType(user.getUid(), type);
        if (mfaOpt.isEmpty()) {
            return Response.error("验证失败");
        }

        val userMfa = mfaOpt.get();
        if (userMfa.getEnabled() == null || !userMfa.getEnabled()) {
            return Response.success(createVerifiedToken(entity, req.getRefreshToken()));
        }

        if (!authenticator.verify(req.getCode(), userMfa.getSecretKey())) {
            return Response.<JwtToken>error("验证码不正确").withType(MessageType.NOTIFY);
        }

        return Response.success(createVerifiedToken(entity, req.getRefreshToken()));
    }

    /**
     * 我的资产交易列表
     */
    @GetMapping("/account/trans")
    @Dictionary
    public Response<List<TransactionVo>> trans(TransQueryDto req, CurrentUser user) {
        val tenantId = "0";
        req.searchCount(false);
        if (StringUtils.isBlank(req.getPageSorts())) {
            req.setPageSorts("createdAt|DESC");
        }

        IPage<UserAccountTransactionEntity> page = userAccountRepository.pageQueryTransactions(req, tenantId,
                user.getUid());
        val trans = page.getRecords();

        return Response.success(this.ucenterConverter.fromTransactions(trans));
    }

    /**
     * 我的账户总览
     */
    @GetMapping("/account/assets")
    public Response<AccountSummaryVo> assets(CurrentUser user) {
        val tenantId = "0";
        val res = userAccountRepository.summary(tenantId, user.getUid(), List.of());
        return Response.success(res);
    }

    /**
     * 获取用户MFA配置
     * @param reset 是否重置已有配置
     * @return 配置响应
     */
    private MfaConfigRes setupUserMfa(@Nonnull UserEntity user, boolean reset) {
        val builder = MfaConfigRes.builder();
        val authenticator = configuration.getAuthenticator().get();

        val type = authenticator.getType();
        val needSave = new AtomicBoolean(reset);
        val mfa = repository.findMfaByUidAndType(user.getId(), type).orElseGet(() -> {
            val userMfa = new UserMfaEntity();
            userMfa.setUid(user.getId());
            userMfa.setAuthType(type);
            userMfa.setSecretKey(authenticator.getSecretKey());
            userMfa.setPhone(user.getPhone());
            userMfa.setEmail(user.getEmail());
            userMfa.setEnabled(false);
            if (!repository.createMfa(userMfa)) {
                throw new CommonBizException("配置MFA时出错了[DB]");
            }
            needSave.set(false);
            return userMfa;
        });

        if (needSave.get()) {
            mfa.setSecretKey(authenticator.getSecretKey());
            mfa.setEnabled(false);
            repository.updateMfa(mfa);
        }

        val config = authenticator.getConfig(user.getUsername(), mfa.getSecretKey(),
                new MapConfig<>(properties.getProps()));
        if (config != null) {
            builder.config(config);
        }
        if (mfa.getSecretKey() != null) {
            builder.secretKey(mfa.getSecretKey());
        }
        builder.initialized(Boolean.TRUE.equals(mfa.getEnabled()));
        builder.type(type);
        return builder.build();
    }

    /**
     * 验证后的TOKEN
     */
    private JwtToken createVerifiedToken(UserEntity user, String refreshToken) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtTokenAuthentication jwtAuth) {
            val userDetails = UserUtils.createUserDetails(user, List.of());
            val jwtToken = jwtAuth.getJwtToken();
            jwtToken.setRefreshToken(refreshToken);
            jwtToken.setMfa(0);

            val newToken = tokenManager.refresh(jwtToken, userDetails);

            jwtAuth.setJwtToken(newToken);
            if (tokenManager instanceof UCenterTokenManager ucenterTokenManager) {
                ucenterTokenManager.setupMultiPoint(jwtAuth);
            }

            return newToken;
        }

        throw TokenException.INVALID_TOKEN;
    }

    /**
     * 验证后的TOKEN
     */
    private JwtToken createRefreshToken(UserEntity user, String refreshToken) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtTokenAuthentication jwtAuth) {
            val userDetails = UserUtils.createUserDetails(user, List.of());
            val jwtToken = jwtAuth.getJwtToken();
            jwtToken.setRefreshToken(refreshToken);
            jwtToken.setCredentialsExpired(false);

            val newToken = tokenManager.refresh(jwtToken, userDetails);

            jwtAuth.setJwtToken(newToken);
            if (tokenManager instanceof UCenterTokenManager ucenterTokenManager) {
                ucenterTokenManager.setupMultiPoint(jwtAuth);
            }

            return newToken;
        }

        throw TokenException.INVALID_TOKEN;
    }

    /**
     * 验证Token，确保Token可被刷新
     */
    private void verifyToken(@Nonnull JwtToken jwtToken) {
        if (jwtToken.getMfa() != null && 0 != jwtToken.getMfa()) {
            throw TokenException.INVALID_TOKEN;
        }
    }

}

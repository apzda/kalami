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
package com.apzda.hajimi.ucenter.infrastructure.mybatis.service;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.hajimi.auditor.AuditContextHolder;
import com.apzda.hajimi.ucenter.controller.admin.dto.UserPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.*;
import com.apzda.hajimi.ucenter.domain.enums.UserStatus;
import com.apzda.hajimi.ucenter.domain.repository.OrganizationRepository;
import com.apzda.hajimi.ucenter.domain.repository.TenantRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.domain.vo.UserVo;
import com.apzda.hajimi.ucenter.event.UserCreatedEvent;
import com.apzda.hajimi.ucenter.event.UserModifiedEvent;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.*;
import com.apzda.hajimi.ucenter.security.token.UCenterTokenManager;
import com.apzda.hajimi.ucenter.service.dto.UserDto;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.apzda.kalami.error.*;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.exception.NotFoundException;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRepositoryImpl extends ServiceImpl<UserEntityMapper, UserEntity> implements UserRepository {

    private final PasswordEncoder passwordEncoder;

    private final OauthEntityMapper oauthEntityMapper;

    private final OauthSessionEntityMapper oauthSessionEntityMapper;

    private final RoleChildrenMapper roleChildrenMapper;

    private final RolePrivilegeMapper rolePrivilegeMapper;

    private final UserRoleMapper userRoleMapper;

    private final UserMfaMapper userMfaMapper;

    private final RoleMapper roleMapper;

    private final OrganizationRepository organizationRepository;

    private final UCenterTokenManager ucenterTokenManager;

    private final ApplicationContext applicationContext;

    private final ApplicationEventPublisher publisher;

    @Override
    public UserEntity findByUserId(Serializable uid) {
        return super.getById(uid);
    }

    @Override
    @Nullable
    public UserEntity findByUsername(String username) {
        return lambdaQuery().eq(UserEntity::getUsername, username).one();
    }

    @Override
    @Nullable
    public UserEntity findByProviderAndAppIdAndUserId(String provider, String appId, Serializable uid) {
        val con = Wrappers.lambdaQuery(OauthEntity.class)
            .eq(OauthEntity::getProvider, provider)
            .eq(OauthEntity::getAppId, appId)
            .eq(OauthEntity::getUid, uid);
        val oauth = oauthEntityMapper.selectOne(con, false);

        if (oauth == null) {
            return null;
        }

        return getById(oauth.getUid());
    }

    @Nullable
    @Override
    public UserEntity findByProviderAndAppIdAndOpenId(String provider, String appId, String openId) {
        val con = Wrappers.lambdaQuery(OauthEntity.class)
            .eq(OauthEntity::getProvider, provider)
            .eq(OauthEntity::getAppId, appId)
            .eq(OauthEntity::getOpenId, openId);
        val oauth = oauthEntityMapper.selectOne(con, false);

        if (oauth == null) {
            return null;
        }

        return getById(oauth.getUid());
    }

    @Override
    @Nullable
    public TenantUserEntity getTenantUserByUid(String tenantId, Long userId) {
        val tenantRepository = applicationContext.getBean(TenantRepository.class);
        return tenantRepository.findUserByTenantAndUid(Long.valueOf(tenantId), userId);
    }

    @Override
    @Nullable
    public OrganizationUserEntity getOrganizationUserByUid(String tenantId, String orgId, Long uid) {
        return organizationRepository.findUserByTidAndOrgIdAndUid(Long.valueOf(tenantId), Long.valueOf(orgId), uid);
    }

    @Override
    public void updateOauthByUidAndProviderAndAppId(@Nonnull OauthEntity oauth, Serializable uid, String provider,
            String appId) {
        try {
            val query = Wrappers.lambdaQuery(OauthEntity.class)
                .eq(OauthEntity::getProvider, provider)
                .eq(OauthEntity::getAppId, appId)
                .eq(OauthEntity::getUid, oauth.getUid());
            val entity = oauthEntityMapper.selectOne(query, false);
            if (entity == null) {
                return;
            }

            oauth.setId(entity.getId());
            oauth.setOpenId(entity.getOpenId());
            oauth.setUnionId(entity.getUnionId());

            if (entity.getLoginTime() != null) {
                return;
            }

            val update = Wrappers.lambdaUpdate(OauthEntity.class)
                .eq(OauthEntity::getProvider, provider)
                .eq(OauthEntity::getAppId, appId)
                .eq(OauthEntity::getUid, oauth.getUid())
                .isNull(OauthEntity::getLoginTime)
                .set(OauthEntity::getLoginTime, oauth.getLoginTime())
                .set(OauthEntity::getIp, oauth.getIp())
                .set(OauthEntity::getDevice, oauth.getDevice())
                .set(OauthEntity::getUserAgent, oauth.getUserAgent());

            if (oauthEntityMapper.update(update) == 0) {
                log.warn("update oauth by user uid {} failed", oauth.getUid());
            }
        }
        catch (Exception e) {
            log.warn("更新OAuth({})失败: {}", oauth, e.getMessage());
        }
    }

    @Override
    public void createOauthSession(@Nonnull OauthSessionEntity oauthSessionEntity) {
        try {
            oauthSessionEntityMapper.insert(oauthSessionEntity);
        }
        catch (Exception e) {
            log.warn("无法保存登录日志: {} - {}", oauthSessionEntity, e.getMessage());
        }
    }

    @Override
    public OauthSessionEntity getLastLoginSession(Serializable uid, String provider, String appId) {
        val query = Wrappers.lambdaQuery(OauthSessionEntity.class)
            .eq(OauthSessionEntity::getUid, uid)
            .eq(OauthSessionEntity::getProvider, provider)
            .eq(OauthSessionEntity::getAppId, appId)
            .orderByDesc(OauthSessionEntity::getLoginTime)
            .last("LIMIT 1");

        return oauthSessionEntityMapper.selectOne(query);
    }

    @Override
    @Nonnull
    public List<RoleEntity> listUserRoles(Serializable uid, Serializable tenantId, Serializable orgId, boolean all) {
        val roleIds = new HashSet<Serializable>();
        val allRoles = new ArrayList<RoleEntity>();
        val roles = userRoleMapper.listUserRolesByUserIdAndTenantId(uid, tenantId, orgId);

        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        if (all) {
            for (RoleEntity role : roles) {
                val roleId = role.getId();
                allRoles.add(role);
                roleIds.add(roleId);
                findRoleChildren(roleIds, allRoles, roleId, tenantId);
            }

            return allRoles;
        }
        else {
            return roles;
        }
    }

    @Override
    public List<RbacPrivilegeEntity> listPrivilegesByRoleIds(List<Serializable> roleIds, Serializable tenantId) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return List.of();
        }

        return rolePrivilegeMapper.listPrivilegesByRoleIds(roleIds, tenantId);
    }

    @Override
    public Optional<UserMfaEntity> findMfaByUidAndType(Serializable uid, String type) {
        val query = userMfaMapper.selectOne(Wrappers.<UserMfaEntity>lambdaQuery()
            .eq(UserMfaEntity::getUid, uid)
            .eq(UserMfaEntity::getAuthType, type), false);

        return Optional.ofNullable(query);
    }

    @Override
    public boolean createMfa(UserMfaEntity userMfa) {
        return this.userMfaMapper.insert(userMfa) > 0;
    }

    @Override
    public boolean updateMfa(UserMfaEntity mfa) {
        return this.userMfaMapper.updateById(mfa) > 0;
    }

    @Override
    public IPage<UserEntity> pageQuery(UserPageQuery query) {
        IPage<UserEntity> pager = PageUtil.from(query);
        return this.baseMapper.pageQuery(pager, query);
    }

    @Override
    @Transactional
    public UserEntity create(@Nonnull UserDto dto) {
        val tenantRepository = applicationContext.getBean(TenantRepository.class);
        val organizationRepository = applicationContext.getBean(OrganizationRepository.class);
        val tenantId = dto.getTenantId();
        val orgId = dto.getOrgId();
        TenantEntity tenant = null;

        if (StringUtils.isNotBlank(tenantId)) {
            tenant = tenantRepository.getById(tenantId);
            if (tenant == null) {
                throw new CommonBizException(new ResourceNotFoundError("租户", tenantId));
            }

            if (orgId != null) {
                val organization = organizationRepository.getById(orgId);
                if (organization == null) {
                    throw new CommonBizException(new ResourceNotFoundError("租户", orgId));
                }
                if (!organization.getTenantId().equals(tenantId)) {
                    throw new CommonBizException(
                            String.format("部门【%s】不属于租户【%s】", organization.getName(), tenant.getName()));
                }
            }
        }

        val username = dto.getUsername();
        val user = findByUsername(username);
        if (user != null) {
            // 账户已存在
            if (tenant != null) {
                var tu = tenantRepository.findUserByTenantAndUid(0L, user.getId());
                if (tu != null) {
                    // 确保不能添加平台用户
                    throw new CommonBizException(new DuplicateError(username));
                }

                tu = tenantRepository.findUserByTenantAndUid(tenant.getId(), user.getId());
                if (tu == null) {
                    // 加入到租户中
                    tu = tenantRepository.join(tenant.getId(), user.getId());
                    if (tu == null) {
                        throw new CommonBizException(new DuplicateError(username));
                    }
                    // 角色
                    val roles = dto.getRoles();
                    if (!CollectionUtils.isEmpty(roles)) {
                        updateRoles(roles, user, tenantId);
                    }
                }

                if (orgId != null) {
                    val ouId = Long.parseLong(orgId);
                    var ou = organizationRepository.findUserByTidAndOrgIdAndUid(tenant.getId(), ouId, user.getId());
                    if (ou == null) {
                        ou = organizationRepository.join(tenant.getId(), ouId, user.getId());
                    }
                    if (ou == null) {
                        throw new CommonBizException(new DuplicateError(username));
                    }
                }
            }

            // 这个条件
            if (dto.isEnableSmsLogin() && StringUtils.isNotBlank(dto.getPhone())
                    && dto.getPhone().equals(user.getPhone())) {
                bindPhone(user.getId(), dto.getPhone());
            }

            return user;
        }

        UserEntity userEntity = null;
        if (dto.isEnableSmsLogin() && StringUtils.isNotBlank(dto.getPhone())) {
            val oauth = oauthEntityMapper.getByProviderAndOpenId("phone", dto.getPhone());
            if (oauth != null) {
                userEntity = this.getById(oauth.getUid());
                if (userEntity != null) {
                    if (log.isWarnEnabled() && !dto.getPhone().equals(userEntity.getPhone())) {
                        // TBD 此处极有风险，需要一个妥善的方案!
                        log.warn("手机号'{}'与绑定的用户'{}'的手机号'{}'不同", dto.getPhone(), oauth.getUid(), userEntity.getPhone());
                    }
                    return userEntity;
                }
            }
        }

        userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setNickname(dto.getNickname());
        if (StringUtils.isNotBlank(dto.getPassword())) {
            userEntity.setPasswd(passwordEncoder.encode(dto.getPassword()));
            if (Boolean.TRUE.equals(dto.getResetPassword())) {
                userEntity.setPasswdExpiredAt(0L);
            }
        }
        else {
            userEntity.setPasswd("");
        }

        userEntity.setPhone(dto.getPhone());
        userEntity.setEmail(dto.getEmail());
        userEntity.setStatus(dto.getStatus());
        userEntity.setLanding(dto.getLanding());
        userEntity.setRemark(dto.getRemark());
        userEntity = create(userEntity);

        if (userEntity != null) {
            if (dto.isEnableSmsLogin() && StringUtils.isNotBlank(dto.getPhone())) {
                bindPhone(userEntity.getId(), dto.getPhone());
            }

            if (tenant != null) {
                val uid = userEntity.getId();
                // 租户
                tenantRepository.join(tenant.getId(), uid);
                if (orgId != null) {
                    organizationRepository.join(tenant.getId(), Long.parseLong(orgId), uid);
                }
            }

            // 角色
            val roles = dto.getRoles();
            if (!CollectionUtils.isEmpty(roles)) {
                updateRoles(roles, userEntity, tenantId);
            }
            return userEntity;
        }

        throw new CommonBizException(new ResourceCreateError("用户"));
    }

    @Override
    @Transactional
    public UserEntity update(@Nonnull UserDto dto) {
        val entity = getById(dto.getId());
        if (entity == null) {
            throw new CommonBizException(new ResourceNotFoundError("用户", dto.getId()));
        }
        val userId = entity.getId();
        val tenantId = dto.getTenantId();
        if (tenantId == null) {
            throw new CommonBizException(new NotBlankError("tenantId"));
        }
        val tenantRepository = applicationContext.getBean(TenantRepository.class);
        val tenant = tenantRepository.getById(tenantId);
        if (tenant == null) {
            throw new CommonBizException(new ResourceNotFoundError("租户", tenantId));
        }

        var tu = this.getTenantUserByUid(tenantId, userId);
        if (tu == null) {
            tu = tenantRepository.join(tenant.getId(), userId);
        }

        val user = BeanUtil.copyProperties(dto, UserEntity.class);

        if (!"0".equals(tenantId)) {
            val sysUser = this.getTenantUserByUid("0", userId);
            if (sysUser != null) {
                // 用户同时是平台用户时，不能修改密码、状态
                dto.setPassword(null);
                user.setRemark(null);
                user.setStatus(null);
                user.setPhone(null);
                user.setEmail(null);
            }
            if (tenant.getUid().equals(tu.getUid())) {
                // 租户管理员必须有sa角色
                user.setStatus(null);
                if (CollectionUtils.isEmpty(dto.getRoles())) {
                    dto.setRoles(List.of("tsa"));
                }
            }
        }

        var passwordChanged = false;
        if (StringUtils.isNotBlank(dto.getPassword())) {
            if (Boolean.TRUE.equals(dto.getResetPassword())) {
                user.setPasswdExpiredAt(0L);
            }
            user.setPasswd(passwordEncoder.encode(dto.getPassword()));
            passwordChanged = true;
        }

        if (1L == user.getId()) {
            user.setStatus(null); // 超级管理员不能修改状态
            if (CollectionUtils.isEmpty(dto.getRoles())) {
                // 超级管理员必须有sa角色
                dto.setRoles(List.of("sa"));
            }
        }

        if (updateById(user)) {
            if (dto.isEnableSmsLogin() && StringUtils.isNotBlank(dto.getPhone())
                    && !dto.getPhone().equals(entity.getPhone())) {
                bindPhone(user.getId(), dto.getPhone());
            }

            val orgId = dto.getOrgId();
            if (orgId != null) {
                val ou = this.getOrganizationUserByUid(tenantId, orgId, userId);
                if (ou == null) {
                    organizationRepository.join(Long.parseLong(tenantId), Long.parseLong(orgId), userId);
                }
            }

            this.updateRoles(dto.getRoles(), user, tenantId);
            if (passwordChanged) {
                kickoff(userId);
            }

            val vo = new UserVo();
            BeanUtil.copyProperties(entity, vo);
            publisher.publishEvent(new UserModifiedEvent(vo));
            BeanUtil.copyProperties(user, entity);
            return entity;
        }

        throw new CommonBizException(new ResourceUpdateError("用户", dto.getId()));
    }

    @Override
    public void updateBase(UserDto dto) {
        val user = BeanUtil.copyProperties(dto, UserEntity.class);
        if (1L == user.getId()) {
            user.setStatus(null);
        }

        var passwordChanged = false;

        if (StringUtils.isNotBlank(dto.getPassword())) {
            if (Boolean.TRUE.equals(dto.getResetPassword())) {
                user.setPasswdExpiredAt(0L);
            }
            user.setPasswd(passwordEncoder.encode(dto.getPassword()));
            passwordChanged = true;
        }

        if (updateById(user)) {
            if (passwordChanged) {
                kickoff(dto.getId());
            }

            dto.setPassword(null);
        }
    }

    @Override
    public boolean unlockUsers(String tenantId, List<Serializable> ids) {
        val uids = getRealTenantUserIds(tenantId, ids);
        if (CollectionUtils.isEmpty(uids)) {
            return true;
        }
        AuditContextHolder.getContext().setNewValue(uids);
        return lambdaUpdate().in(UserEntity::getId, uids)
            .eq(UserEntity::getStatus, UserStatus.DISABLED)
            .set(UserEntity::getStatus, UserStatus.ACTIVATED)
            .update();
    }

    @Override
    public boolean lockUsers(String tenantId, List<Serializable> ids) {
        val uids = getRealTenantUserIds(tenantId, ids);
        if (CollectionUtils.isEmpty(uids)) {
            return true;
        }

        AuditContextHolder.getContext().setNewValue(uids);
        val rst = lambdaUpdate().in(UserEntity::getId, uids)
            .eq(UserEntity::getStatus, UserStatus.ACTIVATED)
            .set(UserEntity::getStatus, UserStatus.DISABLED)
            .update();

        if (rst) {
            for (Serializable uid : uids) {
                kickoff(uid);
            }
        }

        return rst;
    }

    @Override
    public void grantRoles(Long uid, List<RoleEntity> roles) {
        if (roles == null || roles.isEmpty()) {
            return;
        }
        List<UserRoleEntity> userRoles = new ArrayList<>();
        for (RoleEntity role : roles) {
            if (userRoleMapper.isGranted(uid, role.getId(), role.getTenantId())) {
                continue;
            }
            UserRoleEntity entity = new UserRoleEntity();
            entity.setTenantId(role.getTenantId());
            entity.setUid(uid);
            entity.setRoleId(role.getId());
            userRoles.add(entity);
        }

        if (!userRoles.isEmpty()) {
            userRoleMapper.insert(userRoles);
        }
    }

    @Override
    public void revokeAllByRole(@Nonnull RoleEntity role) {
        userRoleMapper.deleteByRoleId(role.getId(), role.getTenantId());
    }

    @Override
    public void kickoff(Serializable uid) {
        try {
            List<OauthEntity> logins = oauthEntityMapper.listByUid(uid);
            if (!CollectionUtils.isEmpty(logins)) {
                logins.forEach(login -> {
                    val provider = login.getProvider();
                    val appId = login.getAppId();
                    ucenterTokenManager.remove(String.valueOf(uid), provider, appId);
                });
            }
        }
        catch (Exception e) {
            log.warn("踢用户下线失败: {} - {}", uid, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void bindPhone(Long uid, String phone) {
        val user = getById(uid);
        if (user == null) {
            throw new NotFoundException("用户", uid);
        }

        OauthEntity oauth = oauthEntityMapper.getByProviderAndOpenId("phone", phone);
        if (oauth != null) {
            if (oauth.getUid().equals(uid)) {
                return;
            }

            throw new CommonBizException("手机号已占用");
        }

        val oauthEntity = new OauthEntity();
        oauthEntity.setUid(uid);
        oauthEntity.setProvider("phone");
        oauthEntity.setAppId(UserToken.DEFAULT_APPID);
        oauthEntity.setOpenId(phone);

        if (oauthEntityMapper.insert(oauthEntity) == 0) {
            throw new CommonBizException("创建认证信息失败");
        }
    }

    @Nullable
    private UserEntity create(UserEntity user) {
        if (save(user)) {
            val oauth = new OauthEntity();
            oauth.setUid(user.getId());
            oauth.setProvider(UserToken.DEFAULT_PROVIDER);
            oauth.setAppId(UserToken.DEFAULT_APPID);
            oauth.setOpenId(user.getUsername());

            if (oauthEntityMapper.insert(oauth) > 0) {
                val vo = new UserVo();
                BeanUtil.copyProperties(user, vo);
                publisher.publishEvent(new UserCreatedEvent(vo));
                return user;
            }
        }

        return null;
    }

    /**
     * 保存账户角色信息.
     */
    private void updateRoles(List<String> roleIds, @Nonnull UserEntity user, String tenantId) {
        if (roleIds == null) {
            return;
        }

        val userId = user.getId();

        List<RoleEntity> granted = listUserRoles(userId, tenantId, null, false);
        val context = AuditContextHolder.getContext();
        context.setOldValue(granted.stream().map(r -> String.format("[%s]%s", r.getRole(), r.getName())).toList());

        if (CollectionUtils.isEmpty(roleIds)) {
            userRoleMapper.delete(Wrappers.lambdaQuery(UserRoleEntity.class)
                .eq(UserRoleEntity::getTenantId, tenantId)
                .eq(UserRoleEntity::getUid, userId));
            return;
        }
        val newRoles = new ArrayList<String>();
        val roles = new ArrayList<UserRoleEntity>();
        for (val rid : roleIds) {
            val role = roleMapper.selectOne(Wrappers.lambdaQuery(RoleEntity.class)
                .eq(RoleEntity::getRole, rid)
                .eq(RoleEntity::getTenantId, tenantId));
            // 角色存在
            if (role != null) {
                newRoles.add(String.format("[%s]%s", role.getRole(), role.getName()));
                if (!userRoleMapper.isGranted(userId, role.getId(), tenantId)) {
                    // 新的授权
                    val userRole = new UserRoleEntity();
                    userRole.setTenantId(tenantId);
                    userRole.setRoleId(role.getId());
                    userRole.setUid(userId);
                    roles.add(userRole);
                }
                if (!granted.isEmpty()) {
                    // 从已授权列表中排除（后边会删除已授权列表的)
                    granted = granted.stream().filter((r) -> !r.getRole().equals(rid)).toList();
                }
            }
        }

        if (userId == 1L) {
            // 不能把超级管理的sa角色回收了
            granted = granted.stream().filter(r -> !r.getRole().equals("sa")).toList();
        }
        if (!granted.isEmpty()) {
            // 收回角色
            for (RoleEntity role : granted) {
                userRoleMapper.revoke(userId, role.getId(), tenantId);
            }
        }

        if (!roles.isEmpty()) {
            // 分配角色
            userRoleMapper.insert(roles);
        }
        context.setNewValue(newRoles);
    }

    // 填充roleId的孩子角色
    private void findRoleChildren(@Nonnull Set<Serializable> roleIds, @Nonnull List<RoleEntity> allRoles,
            @Nonnull Serializable roleId, Serializable tenantId) {
        List<RoleEntity> children = roleChildrenMapper.listChildren(roleId, tenantId);
        if (!CollectionUtils.isEmpty(children)) {
            for (RoleEntity child : children) {
                val rid = child.getId();
                if (roleIds.contains(rid)) {
                    continue;
                }
                allRoles.add(child);
                roleIds.add(rid);
                findRoleChildren(roleIds, allRoles, rid, tenantId);
            }
        }
    }

    @Nonnull
    private List<Serializable> getRealTenantUserIds(String tenantId, @Nonnull Collection<Serializable> ids) {
        if ("0".equals(tenantId)) {
            return ids.stream().toList();
        }

        val tenantRepository = applicationContext.getBean(TenantRepository.class);
        val uids = new ArrayList<Serializable>();

        new HashSet<>(ids).forEach(uid -> {
            val tenants = tenantRepository.listTenantByUid((String) uid);
            if (tenants.stream().anyMatch(tu -> tu.getTenantId() == 0L)) {
                return;
            }

            uids.add(uid);
        });

        return uids;
    }

}

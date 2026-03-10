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

package com.apzda.hajimi.ucenter.domain.repository;

import com.apzda.hajimi.ucenter.controller.admin.dto.UserPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.*;
import com.apzda.hajimi.ucenter.service.dto.UserDto;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.repository.IRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
public interface UserRepository extends IRepository<UserEntity> {

    @Nullable
    UserEntity findByUserId(Serializable uid);

    @Nullable
    UserEntity findByUsername(String username);

    @Nullable
    UserEntity findByProviderAndAppIdAndUserId(String provider, String appId, Serializable uid);

    @Nullable
    UserEntity findByProviderAndAppIdAndOpenId(String provider, String appId, String openId);

    @Nullable
    TenantUserEntity getTenantUserByUid(String tenantId, Long userId);

    @Nullable
    OrganizationUserEntity getOrganizationUserByUid(String tenantId, String orgId, Long uid);

    /**
     * 更新认证信息
     */
    void updateOauthByUidAndProviderAndAppId(@Nonnull OauthEntity oauth, Serializable uid, String provider,
            String appId);

    /**
     * 创建认证会话
     */
    void createOauthSession(@Nonnull OauthSessionEntity oauthSessionEntity);

    /**
     * 获取最新的登录会话信息
     */
    OauthSessionEntity getLastLoginSession(Serializable uid, String provider, String appId);

    @Nonnull
    default List<RoleEntity> listUserRoles(Serializable uid, Serializable tenantId) {
        return listUserRoles(uid, tenantId, null, true);
    }

    /**
     * 获取用户角色
     */
    @Nonnull
    List<RoleEntity> listUserRoles(Serializable uid, Serializable tenantId, Serializable orgId, boolean all);

    /**
     * 获取角色的授权列表
     */
    List<RbacPrivilegeEntity> listPrivilegesByRoleIds(List<Serializable> roleIds, Serializable tenantId);

    /**
     * 获取用户的多重因素配置
     */
    Optional<UserMfaEntity> findMfaByUidAndType(Serializable uid, String type);

    /**
     * 创建新的多重因素配置
     */
    boolean createMfa(UserMfaEntity userMfa);

    /**
     * 更新多因素认证配置
     */
    boolean updateMfa(UserMfaEntity mfa);

    /**
     * 分页查询
     */
    IPage<UserEntity> pageQuery(UserPageQuery query);

    /**
     * 创建用户
     */
    UserEntity create(UserDto dto);

    /**
     * 修改用户
     */
    UserEntity update(UserDto dto);

    /**
     * 修改用户基本属性，不更新角色
     */
    void updateBase(UserDto dto);

    /**
     * 启用用户
     */
    boolean unlockUsers(String tenantId, List<Serializable> ids);

    /**
     * 禁用用户
     */
    boolean lockUsers(String tenantId, List<Serializable> ids);

    /**
     * 分配角色
     */
    void grantRoles(Long uid, List<RoleEntity> roles);

    /**
     * 收回所有人的角色
     */
    void revokeAllByRole(@Nonnull RoleEntity role);

    /**
     * 踢下线
     */
    void kickoff(Serializable uid);

    /**
     * 绑定手机号
     */
    void bindPhone(Long uid, String phone);

}

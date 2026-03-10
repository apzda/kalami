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

package com.apzda.hajimi.ucenter.domain.vo;

import com.apzda.hajimi.ucenter.domain.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVo {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 创建用户
     */
    private String createdBy;

    /**
     * 最后修改时间
     */
    private Long updatedAt;

    /**
     * 最后修改用户
     */
    private String updatedBy;

    /**
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 落地页
     */
    private String landing;

    /**
     * 状态
     */
    private UserStatus status;

    /**
     * 账户过期时间(null永不过期)
     */
    private Long expiredAt;

    /**
     * 密码过期时间(null永不过期)
     */
    private Long passwdExpiredAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 所属租户ID
     */
    private String tenantId;

    /**
     * 所属部门ID
     */
    private String orgId;

    /**
     * 角色列表
     */
    private List<RoleVo> roles;

    /**
     * 权限列表
     */
    private List<PrivilegeVo> privileges;

    /**
     * 用户元数据
     */
    private List<UserMetaVo> metas;

}

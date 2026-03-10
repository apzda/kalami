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
package com.apzda.hajimi.ucenter.service.dto;

import com.apzda.hajimi.ucenter.domain.enums.UserStatus;
import com.apzda.hajimi.ucenter.service.UCenterService;
import com.apzda.kalami.data.validation.Group;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class UserDto {

    /**
     * 用户ID
     */
    @NotNull(groups = Group.Update.class)
    @Null(groups = Group.New.class)
    private Long id;

    /**
     * 租户ID
     */
    @NotNull(groups = UCenterService.class)
    private String tenantId;

    /**
     * 组织ID
     */
    @NotNull(groups = UCenterService.class)
    private String orgId;

    /**
     * 第一次登录时是否需要重置密码
     */
    @JsonProperty("resetPassword")
    private Boolean resetPassword;

    /**
     * 角色列表
     */
    @JsonProperty("roles")
    private List<String> roles;

    /**
     * 状态
     */
    @JsonProperty("status")
    private UserStatus status;

    /**
     * 用户名
     */
    @JsonProperty("username")
    @NotBlank(groups = Group.New.class)
    private String username;

    /**
     * 昵称
     */
    @JsonProperty("nickname")
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 密码
     */
    @JsonProperty("password")
    private String password;

    /**
     * 确认密码
     */
    @JsonProperty("confirmPassword")
    private String confirmPassword;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 落地页
     */
    private String landing;

    /**
     * 启用短信登录
     */
    private boolean enableSmsLogin;

}

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
package com.apzda.hajimi.ucenter.controller.admin.dto;

import com.apzda.kalami.data.validation.Group;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class PrivilegeDto {

    /**
     * 权限ID
     */
    @NotNull(groups = Group.Update.class)
    @Null(groups = Group.New.class)
    private Long id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 权限名称
     */
    @NotBlank
    private String name;

    /**
     * 权限类型
     */
    @NotBlank(groups = Group.New.class)
    private String type;

    /**
     * 权限表达式(action:resource)
     */
    @NotBlank
    private String permission;

    /**
     * 额外配置
     */
    private String extra;

    /**
     * 说明
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

}

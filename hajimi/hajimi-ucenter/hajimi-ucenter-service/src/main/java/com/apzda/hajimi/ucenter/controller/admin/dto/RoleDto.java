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

import java.util.ArrayList;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class RoleDto {

    /**
     * 角色ID
     */
    @NotNull(groups = Group.Update.class)
    @Null(groups = Group.New.class)
    private Long id;

    /**
     * 角色ID
     */
    @NotBlank(groups = Group.New.class)
    private String role;

    /**
     * 角色名
     */
    @NotBlank
    private String name;

    /**
     * 落地页
     */
    private String landing;

    /**
     * 可租赁
     */
    private Boolean leasable;

    /**
     * 说明
     */
    private String description;

    /**
     * 子角色ID
     */
    private List<Long> children = new ArrayList<>();

    /**
     * 分配的权限
     */
    private List<Long> granted = new ArrayList<>();

    /**
     * 租户ID
     */
    private String tenantId;

}

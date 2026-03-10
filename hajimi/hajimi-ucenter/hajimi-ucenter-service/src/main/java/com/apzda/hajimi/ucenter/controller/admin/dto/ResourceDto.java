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
public class ResourceDto {

    /**
     * 资源ID
     */
    @NotNull(groups = Group.Update.class)
    @Null(groups = Group.New.class)
    private Long id;

    /**
     * 上级资源ID
     */
    @NotNull(groups = Group.New.class)
    private Long pid;

    /**
     * 资源标识
     */
    @NotBlank
    private String rid;

    /**
     * 资源名称
     */
    @NotBlank
    private String name;

    @NotBlank
    private String service;

    /**
     * 支持的操作列表(逗号分隔)
     */
    private String actions;

    /**
     * 资源ID浏览器类的全类名(FQDN)
     */
    private String explorer;

    /**
     * 资源说明
     */
    private String description;

}

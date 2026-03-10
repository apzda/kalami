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
package com.apzda.hajimi.ucenter.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/**
 * RBAC 资源表
 */
@TableName(value = "ucenter_rbac_resource")
@Data
public class RbacResourceEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableLogic
    private Boolean deleted;

    /**
     * 上级资源ID
     */
    private Long pid;

    /**
     * 资源标识
     */
    private String rid;

    /**
     * 所属可订阅服务
     */
    private String service;

    /**
     * 父级资源ID
     */
    @TableField(exist = false)
    private String parentRid;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 父级资源名称
     */
    @TableField(exist = false)
    private String parentName;

    /**
     * 支持的操作列表(逗号分隔)
     */
    private String actions;

    /**
     * 内置资源
     */
    private Boolean builtin;

    /**
     * 资源ID浏览器类的全类名(FQDN)
     */
    private String explorer;

    /**
     * 资源说明
     */
    private String description;

    @TableField(exist = false)
    private Integer childrenCount;

}

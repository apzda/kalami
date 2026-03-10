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

import com.apzda.hajimi.ucenter.domain.enums.PrivilegeType;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 权限列表
 */
@TableName(value = "ucenter_rbac_privilege")
@Data
public class RbacPrivilegeEntity implements Serializable {

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
     * 租户ID
     */
    private String tenantId;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限类型
     */
    private PrivilegeType type;

    /**
     * 是否是内置权限
     */
    private Boolean builtin;

    /**
     * 所属可订阅服务
     */
    private String service;

    /**
     * 权限表达式(action:resource)
     */
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

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RbacPrivilegeEntity other)) {
            return false;
        }

        return Objects.equals(tenantId, other.tenantId) && Objects.equals(permission, other.permission);
    }

    public int hashCode() {
        return Objects.hash(tenantId, permission);
    }

}

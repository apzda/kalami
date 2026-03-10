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

import cn.hutool.core.date.LocalDateTimeUtil;
import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.kalami.tenant.Tenant;
import com.baomidou.mybatisplus.annotation.*;
import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.val;

import java.io.Serializable;

/**
 * 租户表
 */
@TableName(value = "ucenter_tenant")
@Data
public class TenantEntity implements Serializable {

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
     * 上级ID
     */
    private Long parentId;

    /**
     * 代理ID
     */
    private Long agentId;

    /**
     * 租户代码
     */
    private String code;

    /**
     * 租户全称
     */
    private String name;

    /**
     * 租户简称
     */
    private String shortName;

    /**
     * 联系人姓名
     */
    private String contact;

    /**
     * 联系地址
     */
    private String address;

    /**
     * 租户头像
     */
    private String logo;

    /**
     * 租户超级管理员ID
     */
    private Long uid;

    /**
     * 租户状态
     */
    private TenantStatus status;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;

    @Nonnull
    public Tenant toTenant() {
        val source = new Tenant();
        source.setId(String.valueOf(this.getId()));
        source.setPid(String.valueOf(this.getParentId()));
        source.setAgentId(String.valueOf(this.getAgentId()));
        source.setCode(this.getCode());
        source.setName(this.getName());
        source.setShortName(this.getShortName());
        source.setLogo(this.getLogo());
        source.setContact(this.getContact());
        source.setAddress(this.getAddress());
        source.setUid(String.valueOf(this.getUid()));
        source.setPhone(this.getPhone());
        source.setCreateTime(LocalDateTimeUtil.of(this.getCreatedAt()));
        source.setCurrent(false);

        return source;
    }

}

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

import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.kalami.agent.Agent;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * 代理商表
 */
@TableName(value = "ucenter_agent", autoResultMap = true)
@Data
public class AgentEntity implements Serializable {

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
     * 上级ID
     */
    private Long parentId;

    /**
     * 代理商全称
     */
    private String name;

    /**
     * 代理商简称
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
     * 代理商用户ID
     */
    private Long uid;

    /**
     * 代理商状态
     */
    private TenantStatus status;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 费率配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Double> ratio;

    /**
     * 商户号
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> merNo;

    /**
     * 备注
     */
    private String remark;

    public Agent toAgent() {
        Agent a = new Agent();
        a.setId(String.valueOf(this.getId()));
        a.setPid(String.valueOf(this.getParentId()));
        a.setName(this.getName());
        a.setShortName(this.getShortName());
        a.setPhone(this.getPhone());
        a.setContact(this.getContact());
        a.setAddress(this.getAddress());
        a.setUid(String.valueOf(this.getUid()));
        a.setRatios(this.getRatio());
        a.setMerNo(Optional.ofNullable(this.getMerNo()).orElse(Map.of()));
        a.setRemark(this.getRemark());
        return a;
    }

}

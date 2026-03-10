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
package com.apzda.hajimi.ucenter.domain.vo;

import com.apzda.hajimi.ucenter.domain.enums.PrivilegeType;
import com.apzda.kalami.dictionary.Dict;
import com.apzda.kalami.tenant.SubscriptionTransformer;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrivilegeVo {

    private Long id;

    private Long createdAt;

    private String createdBy;

    private Long updatedAt;

    private String updatedBy;

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
    @Dict
    private PrivilegeType type;

    /**
     * 是否是内置权限
     */
    private Boolean builtin;

    /**
     * 所属可订阅服务
     */
    @Dict(transformer = SubscriptionTransformer.class)
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

}

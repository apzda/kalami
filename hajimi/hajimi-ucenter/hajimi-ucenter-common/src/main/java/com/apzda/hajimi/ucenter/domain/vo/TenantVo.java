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

import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.kalami.dictionary.Dict;
import com.apzda.kalami.user.UidTransformer;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantVo {

    private Long id;

    private Long createdAt;

    private String createdBy;

    private Long updatedAt;

    private String updatedBy;

    private Boolean deleted;

    /**
     * 上级ID
     */
    @Dict(table = "ucenter_tenant", value = "name")
    private Long parentId;

    /**
     * 代理ID
     */
    @Dict(table = "ucenter_agent", value = "name")
    private Long agentId;

    /**
     * 租户代码
     */
    private String code;

    /**
     * 租户名称
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
    @Dict(transformer = UidTransformer.class, value = "*")
    private Long uid;

    /**
     * 租户状态: 0 - 正常，1 - 冻结
     */
    @Dict
    private TenantStatus status;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;

    private List<TenantVo> children;

}

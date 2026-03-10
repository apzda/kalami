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
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentVo {

    private Long id;

    private Long createdAt;

    private String createdBy;

    private Long updatedAt;

    private String updatedBy;

    private Boolean deleted;

    /**
     * 上级ID
     */
    @Dict(table = "ucenter_agent", code = "id", value = "name")
    private Long parentId;

    /**
     * 代理商名称
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
     * 代理商超级管理员ID
     */
    @Dict(transformer = UidTransformer.class, value = "*")
    private Long uid;

    /**
     * 代理商状态: 0 - 正常，1 - 冻结
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

    /**
     * 费率配置
     */
    private Map<String, Double> ratio;

    /**
     * 商户号
     */
    private Map<String, String> merNo;

    private List<AgentVo> children;

}

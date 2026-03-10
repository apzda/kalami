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

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class OrganizationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = -2093831068536794095L;

    private Long id;

    private Long createdAt;

    private String createdBy;

    private Long updatedAt;

    private String updatedBy;

    private Boolean deleted;

    /**
     * 部门ID
     */
    private Long orgId;

    /**
     * Tenant id
     */
    private String tenantId;

    /**
     * 上级组织ID
     */
    private Long parentId;

    /**
     * 组织部门代码
     */
    private String code;

    /**
     * 组织部门名称
     */
    private String name;

    /**
     * 组织部门简称
     */
    private String shortName;

    /**
     * 组织部门头像
     */
    private String logo;

    /**
     * 备注
     */
    private String remark;

    /**
     * User ID
     */
    private Long uid;

    /**
     * 是否是用户默认选择的租户
     */
    private Boolean current;

    /**
     * 下级部门
     */
    private List<OrganizationVo> children;

}

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
package com.apzda.hajimi.ucenter.service.dto;

import com.apzda.kalami.data.validation.Group;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class OrganizationDto {

    /**
     * 组织ID(更新时需要)
     */
    @NotNull(groups = Group.Update.class)
    @Null(groups = Group.New.class)
    private Long id;

    /**
     * 上级ID
     */
    private Long parentId;

    /**
     * 组织编码
     */
    @NotBlank
    @Length(max = 12, min = 1)
    private String code;

    @NotBlank
    private String tenantId;

    /**
     * 组织名称
     */
    @NotBlank
    private String name;

    /**
     * 组织简称
     */
    private String shortName;

    /**
     * 组织头像
     */
    private String logo;

    /**
     * 备注
     */
    private String remark;

    public String getShortName() {
        if (StringUtils.isBlank(shortName)) {
            return StringUtils.substring(name, 0, 5);
        }
        return shortName;
    }

}

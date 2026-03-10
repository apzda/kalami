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

import cn.hutool.core.date.DatePattern;
import com.apzda.kalami.data.validation.Group;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class SimpleTenantDto {

    /**
     * 租户ID
     */
    private String tenantId;

    public String getTenantId() {
        return Optional.ofNullable(this.tenantId).orElse("0");
    }

    /**
     * 代理ID
     */
    @Min(value = 0, message = "非法的代理ID")
    private Long agentId;

    public Long getAgentId() {
        return Optional.ofNullable(this.agentId).orElse(0L);
    }

    /**
     * 租户名称
     */
    @NotBlank(groups = Group.New.class)
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
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^(\\+\\d{2,5}-)?1\\d{10}$", message = "手机号码格式不正确")
    private String phone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订阅
     */
    @Valid
    private List<Service> services;

    public String getShortName() {
        if (StringUtils.isBlank(shortName)) {
            return StringUtils.substring(name, 0, 5);
        }
        return shortName;
    }

    @Data
    public static class Service {

        /**
         * ID
         */
        @NotBlank(message = "服务ID不能为空")
        private String id;

        /**
         * 过期时间
         */
        @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
        private LocalDateTime expireTime;

    }

}

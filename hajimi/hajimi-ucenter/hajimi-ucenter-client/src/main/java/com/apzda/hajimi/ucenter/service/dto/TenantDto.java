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
import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.kalami.data.validation.Group;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class TenantDto {

    /**
     * 租户ID
     */
    @NotNull(groups = Group.Update.class)
    @Null(groups = Group.New.class)
    private Long id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 代理ID
     */
    @Min(value = 0, message = "非法的代理ID")
    private Long agentId;

    public Long getAgentId() {
        return Optional.ofNullable(this.agentId).orElse(0L);
    }

    /**
     * 租户编码
     */
    @NotBlank(groups = Group.New.class)
    @Length(max = 12, min = 1)
    private String code;

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
     * 租户状态
     */
    private TenantStatus status;

    /**
     * 联系电话
     */
    @NotBlank(groups = Group.New.class)
    @Pattern(regexp = "^(\\+\\d{2,5}-)?1\\d{10}$", message = "手机号码格式不正确")
    private String phone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 超管用户名
     */
    @NotBlank(groups = Group.New.class)
    private String username;

    /**
     * 超管姓名
     */
    private String nickname;

    /**
     * 超管密码
     */
    @NotBlank(groups = Group.New.class)
    private String password;

    /**
     * 确认超管密码
     */
    @NotBlank(groups = Group.New.class)
    private String confirmPassword;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private LocalDateTime expireTime;

    public String getShortName() {
        if (StringUtils.isBlank(shortName)) {
            return StringUtils.substring(name, 0, 5);
        }
        return shortName;
    }

}

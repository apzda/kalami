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

import com.apzda.kalami.data.AbstractPageQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantRequest extends AbstractPageQuery {

    /**
     * 租户ID
     */
    private Long id;

    /**
     * 代理商编码
     */
    private String code;

    /**
     * 上级ID
     */
    private Long parentId;

    /**
     * 代理商ID
     */
    private Long agentId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 商家手机号/租户手机号
     */
    @Pattern(regexp = "^(\\+\\d{2,5}-)?1\\d{10}$", message = "手机号格式错误")
    private String phone;

    /**
     * 商家名称/租户名称-可模糊
     */
    private String name;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startTime;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endTime;

    /**
     * 订阅的服务
     */
    private List<String> subscribed;

}

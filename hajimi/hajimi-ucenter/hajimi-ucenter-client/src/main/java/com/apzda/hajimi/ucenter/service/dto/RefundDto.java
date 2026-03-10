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

import com.apzda.hajimi.ucenter.domain.enums.Asset;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class RefundDto {

    /**
     * 租户ID，默认为"0"
     */
    private String tenantId;

    /**
     * 用户ID
     */
    @Min(value = 1, message = "用户ID不能小于1")
    private long uid;

    /**
     * 资产类型
     */
    @NotNull(message = "资产类型不能为空")
    private Asset asset;

    /**
     * 业务线
     */
    @NotBlank(message = "业务线不能为空")
    private String bizType;

    /**
     * 业务单号
     */
    @NotBlank(message = "业务单号不能为空")
    private String orderNo;

    /**
     * 业务主题
     */
    @NotBlank(message = "退款主题不能为空")
    private String subject;

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "最少退0.01")
    private BigDecimal amount;

    /**
     * 原交易ID
     */
    @NotNull(message = "原交易ID不能为空")
    @Min(value = 1, message = "非法的交易ID")
    private Long transId;

    /**
     * 说明
     */
    private String remark;

    /**
     * 退款原因
     */
    private String reason;

    public String getTenantId() {
        return StringUtils.defaultIfBlank(this.tenantId, "0");
    }

}

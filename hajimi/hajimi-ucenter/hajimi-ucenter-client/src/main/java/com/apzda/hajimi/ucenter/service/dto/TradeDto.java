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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 交易数据传输对象
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class TradeDto {

    /**
     * 是否是预交易
     */
    private boolean advance;

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
     * 账户资产类型
     */
    @NotNull(message = "资产类型不能为空")
    private Asset asset;

    /**
     * 交易发生额
     */
    @NotNull(message = "交易发生额不能为空")
    private BigDecimal amount;

    /**
     * 交易业务类型(线)
     */
    @NotBlank(message = "交易业务类型(线)不能为空")
    @Length(max = 20, message = "交易业务类型(线)最多20个字符")
    private String bizType;

    /**
     * 业务描述
     */
    @NotBlank(message = "业务描述不能为空")
    @Length(max = 32, message = "业务描述最多32个字符")
    private String bizSubject;

    /**
     * 交易业务订单编号
     */
    @NotBlank(message = "交易业务订单编号不能为空")
    @Length(max = 32, message = "交易业务订单编号最多32个字符")
    private String bizOrderNo;

    /**
     * 交易备注
     */
    @Length(max = 256, message = "交易备注最多256个字符")
    private String remark;

    /**
     * 额外数据
     */
    private Map<String, Object> extra;

    public String getTenantId() {
        return StringUtils.defaultIfBlank(this.tenantId, "0");
    }

}

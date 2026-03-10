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
package com.apzda.hajimi.ucenter.service.vo;

import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.kalami.dictionary.Dict;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = -787417766345240040L;

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
     * 用户ID
     */
    private Long uid;

    /**
     * 账户资产类型
     */
    @Dict
    private Asset asset;

    /**
     * 交易发生额
     */
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    private BigDecimal preBalance;

    /**
     * 交易后余额
     */
    private BigDecimal balance;

    /**
     * 交易前冻结金额
     */
    private BigDecimal preFrozen;

    /**
     * 交易后冻结金额
     */
    private BigDecimal frozen;

    /**
     * 交易业务类型(线)
     */
    private String bizType;

    /**
     * 业务描述
     */
    private String bizSubject;

    /**
     * 交易业务订单编号
     */
    private String bizOrderNo;

    /**
     * 交易摘要
     */
    private String transHash;

    /**
     * 交易备注
     */
    private String remark;

}

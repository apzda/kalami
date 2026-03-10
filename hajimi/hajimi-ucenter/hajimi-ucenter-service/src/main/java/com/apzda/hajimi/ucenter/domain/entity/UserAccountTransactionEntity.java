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
package com.apzda.hajimi.ucenter.domain.entity;

import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.hajimi.ucenter.domain.enums.TransStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.springframework.util.DigestUtils;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 资产交易流水记录表
 */
@TableName(value = "ucenter_user_account_transaction")
@Data
public class UserAccountTransactionEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -5597315343903980648L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableLogic
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
     * 预交易ID
     */
    private Long transitId;

    /**
     * 交易摘要
     */
    private String transHash;

    /**
     * 交易备注
     */
    private String remark;

    @TableField(exist = false)
    private TransStatus status;

    public String getTransHash() {
        return DigestUtils
            .md5DigestAsHex(String.format("%s%s%s%s%s", tenantId, uid, asset, bizType, bizOrderNo).getBytes());
    }

}

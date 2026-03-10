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
package com.apzda.hajimi.ucenter.domain.repository;

import com.apzda.hajimi.ucenter.domain.entity.UserAccountEntity;
import com.apzda.hajimi.ucenter.domain.entity.UserAccountRefundEntity;
import com.apzda.hajimi.ucenter.domain.entity.UserAccountTransactionEntity;
import com.apzda.hajimi.ucenter.domain.entity.UserAccountTransitionEntity;
import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.hajimi.ucenter.service.dto.RefundDto;
import com.apzda.hajimi.ucenter.service.dto.TradeDto;
import com.apzda.hajimi.ucenter.service.dto.TransQueryDto;
import com.apzda.hajimi.ucenter.service.dto.TransitionDto;
import com.apzda.hajimi.ucenter.service.vo.AccountSummaryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.repository.IRepository;
import jakarta.annotation.Nonnull;

import java.io.Serializable;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface UserAccountRepository extends IRepository<UserAccountEntity> {

    /**
     * 获取用户资产账户
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param asset 资产类型
     */
    @Nonnull
    UserAccountEntity getByTenantIdAndUidAndAsset(Serializable tenantId, Serializable uid, @Nonnull Asset asset);

    /**
     * 获取交易记录
     * @param tenantId 租户ID
     * @param uid 用户ID
     * @param asset 资产
     * @param bizType 业务线
     * @param bizOrderNo 业务订单号
     */
    UserAccountTransactionEntity getTransactionByTenantIdAndUidAndBiz(Serializable tenantId, Serializable uid,
            Asset asset, String bizType, String bizOrderNo);

    /**
     * 更新用户资产账户
     */
    @Nonnull
    UserAccountEntity updateAccount(@Nonnull UserAccountEntity account);

    /**
     * 交易
     */
    @Nonnull
    UserAccountTransactionEntity trade(TradeDto transaction);

    /**
     * 取消预交易
     */
    UserAccountTransitionEntity cancel(TransitionDto dto);

    /**
     * 取消预交易
     */
    @Nonnull
    UserAccountTransitionEntity cancel(String tenantId, long transactionId, String reason);

    /**
     * 确认预交易
     */
    @Nonnull
    UserAccountTransitionEntity confirm(TransitionDto dto);

    /**
     * 确认预交易
     */
    @Nonnull
    UserAccountTransitionEntity confirm(String tenantId, long transactionId);

    /**
     * 创建交易
     */
    @Nonnull
    UserAccountTransactionEntity trade(UserAccountTransactionEntity trans);

    /**
     * 退款
     */
    @Nonnull
    UserAccountRefundEntity refund(@Nonnull RefundDto dto);

    /**
     * 用户资产概览
     */
    @Nonnull
    AccountSummaryVo summary(Serializable tenantId, Serializable uid, List<Asset> assets);

    /**
     * 获取用户交易记录
     */
    @Nonnull
    IPage<UserAccountTransactionEntity> pageQueryTransactions(TransQueryDto req, Serializable tenantId,
            Serializable uid);

}

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
package com.apzda.hajimi.ucenter.infrastructure.mybatis.service;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.hajimi.ucenter.domain.entity.*;
import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.hajimi.ucenter.domain.enums.TransStatus;
import com.apzda.hajimi.ucenter.domain.repository.*;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.UserAccountMapper;
import com.apzda.hajimi.ucenter.service.dto.RefundDto;
import com.apzda.hajimi.ucenter.service.dto.TradeDto;
import com.apzda.hajimi.ucenter.service.dto.TransQueryDto;
import com.apzda.hajimi.ucenter.service.dto.TransitionDto;
import com.apzda.hajimi.ucenter.service.vo.AccountSummaryVo;
import com.apzda.hajimi.ucenter.service.vo.UserAccountVo;
import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.error.ResourceNotFoundError;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountRepositoryImpl extends ServiceImpl<UserAccountMapper, UserAccountEntity>
        implements UserAccountRepository {

    private final UserRepository userRepository;

    private final TenantRepository tenantRepository;

    private final UserAccountTransactionRepository transactionRepository;

    private final UserAccountTransitionRepository transitionRepository;

    private final UserAccountRefundRepository refundRepository;

    @Override
    @Nonnull
    public UserAccountEntity getByTenantIdAndUidAndAsset(Serializable tenantId, Serializable uid,
            @Nonnull Asset asset) {
        Assert.isTrue(tenantId != null, "tenantId must not be null");
        Assert.notNull(asset, "asset must not be null");
        Assert.isTrue(uid != null, "uid must not be null");

        UserEntity user = userRepository.getById(uid);
        if (user == null) {
            throw new CommonBizException(new ResourceNotFoundError("用户", uid));
        }

        var account = lambdaQuery().eq(UserAccountEntity::getTenantId, tenantId)
            .eq(UserAccountEntity::getUid, uid)
            .eq(UserAccountEntity::getAsset, asset)
            .one();

        if (account == null) {
            TenantEntity tenant = tenantRepository.getById(tenantId);
            if (tenant == null) {
                throw new CommonBizException(new ResourceNotFoundError("租户", tenantId));
            }

            account = new UserAccountEntity();
            account.setTenantId(tenantId.toString());
            account.setUid(user.getId());
            account.setAsset(asset);
            account.setBalance(BigDecimal.ZERO);
            account.setFrozen(BigDecimal.ZERO);
            account.setTransition(BigDecimal.ZERO);
            account.setVersion((short) 0);

            try {
                this.save(account);
            }
            catch (Exception e) {
                account = lambdaQuery().eq(UserAccountEntity::getTenantId, tenantId)
                    .eq(UserAccountEntity::getUid, uid)
                    .eq(UserAccountEntity::getAsset, asset)
                    .one();
                if (account == null) {
                    throw new CommonBizException(String.format("无法创建用户(%s@%s)资产账户(%s)", uid, tenantId, asset), e);
                }
            }
        }

        return account;
    }

    @Override
    public UserAccountTransactionEntity getTransactionByTenantIdAndUidAndBiz(Serializable tenantId, Serializable uid,
            Asset asset, String bizType, String bizOrderNo) {

        val con = Wrappers.lambdaQuery(UserAccountTransactionEntity.class)
            .eq(UserAccountTransactionEntity::getTenantId, tenantId)
            .eq(UserAccountTransactionEntity::getUid, uid)
            .eq(UserAccountTransactionEntity::getAsset, asset)
            .eq(UserAccountTransactionEntity::getBizType, bizType)
            .eq(UserAccountTransactionEntity::getBizOrderNo, bizOrderNo);

        return transactionRepository.getOne(con);
    }

    @Override
    @Nonnull
    public UserAccountEntity updateAccount(@Nonnull UserAccountEntity account) {
        Assert.notNull(account, "account must not be null");
        Assert.notNull(account.getTenantId(), "tenantId must greater than 0");
        Assert.notNull(account.getUid(), "uid must greater than 0");
        Assert.notNull(account.getAsset(), "asset must not be null");
        Assert.notNull(account.getVersion(), "version must not be null");

        val con = Wrappers.lambdaQuery(UserAccountEntity.class);
        if (update(account,
                con.eq(UserAccountEntity::getTenantId, account.getTenantId())
                    .eq(UserAccountEntity::getUid, account.getUid())
                    .eq(UserAccountEntity::getAsset, account.getAsset())
                    .eq(UserAccountEntity::getVersion, account.getVersion()))) {
            return account;
        }

        throw new CommonBizException(String.format("更新用户[%s@%s]资产[%s]账户失败", account.getUid(), account.getTenantId(),
                account.getAsset().getDescription()));
    }

    @Override
    @Transactional
    @Nonnull
    public UserAccountTransactionEntity trade(@Nonnull TradeDto trade) {
        if (trade.isAdvance()) {
            return advanceTrade(trade);
        }
        else {
            return normalTrade(trade);
        }
    }

    @Override
    public UserAccountTransitionEntity cancel(@Nonnull TransitionDto dto) {
        Assert.notNull(dto.getUid(), "uid must not be null");
        Assert.notNull(dto.getAsset(), "asset must not be null");
        Assert.notNull(dto.getBizType(), "bizType must not be null");
        Assert.notNull(dto.getBizOrderNo(), "bizOrderNo must not be null");

        val transit = transitionRepository.findByUserIdAndBizTypeAndOrderNo(dto.getTenantId(),
                Long.parseLong(dto.getUid()), dto.getAsset(), dto.getBizType(), dto.getBizOrderNo());
        if (transit == null) {
            throw new CommonBizException(90021, "交易不存在");
        }

        transit.setCancelReason(dto.getReason());
        return cancel(transit);
    }

    @Override
    @Nonnull
    @Transactional
    public UserAccountTransitionEntity cancel(String tenantId, long transactionId, String reason) {
        val transit = transitionRepository.getById(tenantId, transactionId);
        if (transit == null) {
            throw new CommonBizException(90021, "交易不存在");
        }

        transit.setCancelReason(reason);
        return cancel(transit);
    }

    @Override
    @Nonnull
    @Transactional
    public UserAccountTransitionEntity confirm(@Nonnull TransitionDto dto) {
        Assert.notNull(dto.getUid(), "uid must not be null");
        Assert.notNull(dto.getAsset(), "asset must not be null");
        Assert.notNull(dto.getBizType(), "bizType must not be null");
        Assert.notNull(dto.getBizOrderNo(), "bizOrderNo must not be null");

        val transit = transitionRepository.findByUserIdAndBizTypeAndOrderNo(dto.getTenantId(),
                Long.parseLong(dto.getUid()), dto.getAsset(), dto.getBizType(), dto.getBizOrderNo());
        if (transit == null) {
            throw new CommonBizException(90021, "交易不存在");
        }

        return confirm(transit.getTenantId(), transit.getId());
    }

    @Override
    @Nonnull
    @Transactional
    public UserAccountTransitionEntity confirm(String tenantId, long transactionId) {
        val transit = transitionRepository.getById(tenantId, transactionId);
        if (transit == null) {
            throw new CommonBizException(90021, "交易不存在");
        }
        if (transit.getStatus() != TransStatus.PENDING) {
            throw new CommonBizException(90026, "交易状态已完成");
        }

        val account = getByTenantIdAndUidAndAsset(tenantId, transit.getUid(), transit.getAsset());
        val preBalance = account.getBalance();
        val preFrozen = account.getFrozen();
        val amount = transit.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            account.setFrozen(preFrozen.add(amount));
        }
        else {
            val transition = account.getTransition().subtract(amount);
            if (transition.compareTo(BigDecimal.ZERO) < 0) {
                throw new CommonBizException(90026, "账号异常");
            }
            account.setTransition(transition);
            account.setBalance(preBalance.add(amount));
        }
        // 0 更新账户
        this.updateAccount(account);
        // 1 更新预交易->完成
        transit.setStatus(TransStatus.DONE);
        transitionRepository.updateStatus(transit, TransStatus.PENDING);
        // 2 记录交易
        val entity = new UserAccountTransactionEntity();
        entity.setTenantId(tenantId);
        entity.setUid(transit.getUid());
        entity.setAsset(transit.getAsset());
        entity.setAmount(amount);
        entity.setPreBalance(preBalance);
        entity.setBalance(account.getBalance());
        entity.setPreFrozen(preFrozen);
        entity.setFrozen(account.getFrozen());
        entity.setBizType(transit.getBizType());
        entity.setBizSubject(transit.getBizSubject());
        entity.setBizOrderNo(transit.getBizOrderNo());
        entity.setTransitId(transit.getId());
        entity.setRemark(transit.getRemark());

        this.trade(entity);

        return transit;
    }

    @Override
    @Nonnull
    @Transactional
    public UserAccountRefundEntity refund(@Nonnull RefundDto dto) {
        // 0 原交易
        val trans = transactionRepository.getById(dto.getTenantId(), dto.getTransId());
        if (trans == null) {
            throw new CommonBizException(90021, "交易不存在");
        }

        val amount = dto.getAmount();

        // 0 原交易剩余可退金额检测
        BigDecimal refunded = refundRepository.refundedAmount(trans.getTenantId(), trans.getId());
        val refundBalance = trans.getAmount().subtract(refunded).subtract(amount);
        if (refundBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new CommonBizException(90023, "原交易额不足");
        }
        else if (refundBalance.compareTo(trans.getAmount()) > 0) {
            throw new CommonBizException(90023, "错误的退款金额");
        }

        // 1 余额检测
        val account = this.getByTenantIdAndUidAndAsset(trans.getTenantId(), trans.getUid(), trans.getAsset());
        val preFrozen = account.getFrozen();
        val preBalance = account.getBalance();

        if (preBalance.subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new CommonBizException(90022, trans.getAsset().getDescription() + "不足");
        }

        // 2 写退款记录表
        val refund = refundRepository.refund(dto);

        // 3 把退的钱减掉
        account.setBalance(preBalance.subtract(amount));
        this.updateAccount(account);

        // 4 写交易记录
        val t = new UserAccountTransactionEntity();
        t.setTenantId(account.getTenantId());
        t.setUid(account.getUid());
        t.setAsset(trans.getAsset());
        t.setAmount(amount.multiply(BigDecimal.valueOf(-1)));

        t.setPreBalance(preBalance);
        t.setBalance(account.getBalance());

        t.setPreFrozen(preFrozen);
        t.setFrozen(preFrozen); // 本操作冻结金额不变
        t.setBizType(trans.getBizType());
        t.setBizSubject(trans.getBizSubject());
        t.setBizOrderNo(String.valueOf(refund.getId()));
        t.setRemark(trans.getRemark());

        this.trade(t);

        return refund;
    }

    @Override
    @Transactional
    @Nonnull
    public UserAccountTransactionEntity trade(UserAccountTransactionEntity trans) {
        if (transactionRepository.save(trans)) {
            return trans;
        }

        throw new CommonBizException(90020, "创建交易失败");
    }

    @Override
    @Nonnull
    public AccountSummaryVo summary(Serializable tenantId, Serializable uid, List<Asset> assets) {
        val res = new AccountSummaryVo();

        if (uid == null) {
            for (Asset value : Asset.values()) {
                val vo = new UserAccountVo();
                res.getAssets().put(value, vo);
            }
        }
        else {
            val repository = KalamiContextHolder.getBean(this);
            for (Asset value : Asset.values()) {
                val account = repository.getByTenantIdAndUidAndAsset(tenantId, uid, value);
                val vo = new UserAccountVo().setBalance(account.getBalance())
                    .setFrozen(account.getFrozen())
                    .setTransition(account.getTransition());
                res.getAssets().put(value, vo);
            }
        }

        return res;
    }

    @Override
    @Nonnull
    public IPage<UserAccountTransactionEntity> pageQueryTransactions(TransQueryDto req, Serializable tenantId,
            Serializable uid) {
        IPage<UserAccountTransactionEntity> pager = PageUtil.from(req);

        Wrapper<UserAccountTransactionEntity> wrapper = Wrappers.lambdaQuery(UserAccountTransactionEntity.class)
            .eq(UserAccountTransactionEntity::getTenantId, tenantId)
            .eq(UserAccountTransactionEntity::getUid, uid)
            .eq(req.getAsset() != null, UserAccountTransactionEntity::getAsset, req.getAsset())
            .ge(req.getStartDate() != null, UserAccountTransactionEntity::getCreatedAt, req.getStartDate())
            .le(req.getEndDate() != null, UserAccountTransactionEntity::getCreatedAt, req.getEndDate());

        return transactionRepository.page(pager, wrapper);
    }

    @Nonnull
    @Transactional
    public UserAccountTransactionEntity normalTrade(@Nonnull TradeDto trade) {
        val tenantId = trade.getTenantId();
        val uid = trade.getUid();
        val asset = trade.getAsset();
        Assert.notNull(tenantId, "tenantId must not be null");
        Assert.notNull(trade.getUid(), "uid must not be null");
        Assert.notNull(trade.getAsset(), "asset must not be null");

        val amount = trade.getAmount();
        // 0. 获取账户信息
        val account = this.getByTenantIdAndUidAndAsset(tenantId, uid, asset);
        val preFrozen = account.getFrozen();
        val preBalance = account.getBalance();

        // 1. 检测余额
        val balance = preBalance.add(amount);
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new CommonBizException(90022, asset.getDescription() + "不足");
        }

        // 2. 更新账户
        account.setBalance(balance);
        updateAccount(account);

        // 3. 记录交易
        val trans = new UserAccountTransactionEntity();
        trans.setTenantId(account.getTenantId());
        trans.setUid(account.getUid());
        trans.setAsset(asset);
        trans.setAmount(amount);

        trans.setPreBalance(preBalance);
        trans.setBalance(account.getBalance());
        trans.setPreFrozen(preFrozen);
        trans.setFrozen(preFrozen);

        trans.setBizType(trade.getBizType());
        trans.setBizSubject(trade.getBizSubject());
        trans.setBizOrderNo(trade.getBizOrderNo());
        trans.setRemark(trade.getRemark());

        return this.trade(trans);
    }

    /**
     * 预交易
     */
    @Nonnull
    @Transactional
    public UserAccountTransactionEntity advanceTrade(@Nonnull TradeDto trade) {
        val tenantId = trade.getTenantId();
        val uid = trade.getUid();
        val asset = trade.getAsset();
        Assert.notNull(tenantId, "tenantId must not be null");
        Assert.notNull(trade.getUid(), "uid must not be null");
        Assert.notNull(trade.getAsset(), "asset must not be null");

        val amount = trade.getAmount();
        // 0. 获取账户信息
        val account = this.getByTenantIdAndUidAndAsset(tenantId, uid, asset);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            // 预扣款，需要处理frozen与余额
            val preBalance = account.getBalance();
            val preFrozen = account.getFrozen();
            // 1.1 检测余额
            val balance = preBalance.add(amount);
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                throw new CommonBizException(90022, asset.getDescription() + "不足");
            }

            // 1.2 更新账户余额与冰结金额
            account.setBalance(balance);
            account.setFrozen(preFrozen.subtract(amount));
        }
        else {
            // 预加值，需要处理transition
            val transition = account.getTransition();
            account.setTransition(transition.add(amount));
        }

        // 3 更新账户
        this.updateAccount(account);

        // 4 写在途记录
        UserAccountTransactionEntity entity = transactionRepository.getLastTransaction(tenantId, uid, asset);
        UserAccountTransitionEntity trans = new UserAccountTransitionEntity();
        trans.setTenantId(tenantId);
        trans.setUid(uid);
        trans.setAsset(asset);
        trans.setAmount(amount);
        trans.setBizType(trade.getBizType());
        trans.setBizSubject(trade.getBizSubject());
        trans.setBizOrderNo(trade.getBizOrderNo());
        trans.setRemark(trade.getRemark());
        trans.setLastTid(entity == null ? 0L : entity.getId());
        trans.setStatus(TransStatus.PENDING);

        if (transitionRepository.save(trans)) {
            return BeanUtil.copyProperties(trans, UserAccountTransactionEntity.class);
        }

        throw new CommonBizException(90025, "预交易失败");
    }

    @Nonnull
    private UserAccountTransitionEntity cancel(@Nonnull UserAccountTransitionEntity transit) {

        if (transit.getStatus() != TransStatus.PENDING) {
            throw new CommonBizException(90026, "交易状态已完成");
        }
        val tenantId = transit.getTenantId();
        val account = getByTenantIdAndUidAndAsset(tenantId, transit.getUid(), transit.getAsset());
        val preBalance = account.getBalance();
        val preFrozen = account.getFrozen();
        val amount = transit.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            account.setFrozen(preFrozen.add(amount));
            account.setBalance(preBalance.subtract(amount));
        }
        else {
            val transition = account.getTransition().subtract(amount);
            if (transition.compareTo(BigDecimal.ZERO) < 0) {
                throw new CommonBizException(90026, "账号异常");
            }
            account.setTransition(transition);
        }
        this.updateAccount(account);

        // 1 更新预交易->完成
        transit.setStatus(TransStatus.CANCELED);
        transitionRepository.updateStatus(transit, TransStatus.PENDING);

        return transit;
    }

}

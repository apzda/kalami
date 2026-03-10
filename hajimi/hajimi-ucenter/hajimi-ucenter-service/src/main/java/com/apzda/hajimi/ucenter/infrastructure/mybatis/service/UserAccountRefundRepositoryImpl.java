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

import com.apzda.hajimi.ucenter.domain.entity.UserAccountRefundEntity;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountRefundRepository;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.UserAccountRefundMapper;
import com.apzda.hajimi.ucenter.service.dto.RefundDto;
import com.apzda.kalami.exception.CommonBizException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
public class UserAccountRefundRepositoryImpl extends ServiceImpl<UserAccountRefundMapper, UserAccountRefundEntity>
        implements UserAccountRefundRepository {

    @Override
    public BigDecimal refundedAmount(String tenantId, Long transId) {
        return Optional.ofNullable(this.baseMapper.sumRefundedAmount(tenantId, transId)).orElse(BigDecimal.ZERO);
    }

    @Override
    public @Nonnull UserAccountRefundEntity refund(@Nonnull RefundDto dto) {
        val refund = new UserAccountRefundEntity();
        refund.setTenantId(dto.getTenantId());
        refund.setUid(dto.getUid());
        refund.setAsset(dto.getAsset());
        refund.setTransId(dto.getTransId());
        refund.setAmount(dto.getAmount());
        refund.setBizType(dto.getBizType());
        refund.setBizSubject(dto.getSubject());
        refund.setBizOrderNo(dto.getOrderNo());
        refund.setRemark(dto.getRemark());
        refund.setReason(dto.getReason());

        if (save(refund)) {
            return refund;
        }

        throw new CommonBizException(90024, "退款失败");
    }

}

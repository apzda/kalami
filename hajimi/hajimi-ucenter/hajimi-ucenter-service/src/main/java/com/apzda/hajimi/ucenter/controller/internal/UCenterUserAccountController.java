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
package com.apzda.hajimi.ucenter.controller.internal;

import com.apzda.hajimi.ucenter.domain.entity.UserAccountTransitionEntity;
import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.hajimi.ucenter.domain.repository.TenantRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountRepository;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.hajimi.ucenter.service.UserAccountService;
import com.apzda.hajimi.ucenter.service.dto.RefundDto;
import com.apzda.hajimi.ucenter.service.dto.TradeDto;
import com.apzda.hajimi.ucenter.service.dto.TransitionDto;
import com.apzda.hajimi.ucenter.service.vo.RefundVo;
import com.apzda.hajimi.ucenter.service.vo.TransactionVo;
import com.apzda.hajimi.ucenter.service.vo.TransitionVo;
import com.apzda.hajimi.ucenter.service.vo.UserAccountVo;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Primary
public class UCenterUserAccountController implements UserAccountService {

    private final UserAccountRepository userAccountRepository;

    private final TenantRepository tenantRepository;

    private final UCenterConverter ucenterConverter;

    @Override
    public Optional<UserAccountVo> getUserAccount(Asset asset, String uid, String tenantId) {
        val tid = StringUtils.defaultIfBlank(tenantId, "0");
        try {
            val account = userAccountRepository.getByTenantIdAndUidAndAsset(tid, uid, asset);
            return Optional.of(ucenterConverter.from(account));
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<UserAccountVo> listUserAccount(Asset asset, List<String> tenantIds) {
        if (CollectionUtils.isEmpty(tenantIds)) {
            return Collections.emptyList();
        }

        return tenantIds.stream().map((tid) -> {
            val tenant = tenantRepository.getById(tid);
            if (tenant == null) {
                return null;
            }
            val uid = tenant.getUid();
            val account = userAccountRepository.getByTenantIdAndUidAndAsset("0", uid, asset);
            account.setTenantId(tid);
            return ucenterConverter.from(account);
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public Optional<TransactionVo> trade(@Nonnull @Validated TradeDto trade) {
        val trans = userAccountRepository.trade(trade);
        return Optional.of(ucenterConverter.from(trans));
    }

    @Override
    public Optional<RefundVo> refund(@Nonnull @Validated RefundDto dto) {
        val refund = userAccountRepository.refund(dto);
        return Optional.of(ucenterConverter.from(refund));
    }

    @Override
    public Optional<TransitionVo> cancel(@Nonnull @Validated TransitionDto dto) {
        final UserAccountTransitionEntity transit;
        if (dto.getTransId() == null) {
            transit = userAccountRepository.cancel(dto);
        }
        else {
            transit = userAccountRepository.cancel(dto.getTenantId(), dto.getTransId(), dto.getReason());
        }

        return Optional.of(ucenterConverter.from(transit));
    }

    @Override
    public Optional<TransitionVo> confirm(@Nonnull @Validated TransitionDto dto) {
        final UserAccountTransitionEntity transit;
        if (dto.getTransId() == null) {
            transit = userAccountRepository.confirm(dto);
        }
        else {
            transit = userAccountRepository.confirm(dto.getTenantId(), dto.getTransId());
        }

        return Optional.of(ucenterConverter.from(transit));
    }

}

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

import com.apzda.hajimi.ucenter.domain.entity.UserAccountTransitionEntity;
import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.hajimi.ucenter.domain.enums.TransStatus;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountTransitionRepository;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.UserAccountTransitionMapper;
import com.apzda.kalami.exception.CommonBizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
public class UserAccountTransitionRepositoryImpl
        extends ServiceImpl<UserAccountTransitionMapper, UserAccountTransitionEntity>
        implements UserAccountTransitionRepository {

    @Override
    @Nonnull
    public UserAccountTransitionEntity updateStatus(@Nonnull UserAccountTransitionEntity transition,
            @Nonnull TransStatus status) {
        Assert.notNull(transition, "transition must not be null");
        Assert.notNull(status, "status must not be null");
        Assert.notNull(transition.getTenantId(), "tenantId must not be null");
        Assert.notNull(transition.getId(), "id must not be null");
        Assert.notNull(transition.getUid(), "member id must not be null");
        Assert.notNull(transition.getAsset(), "asset must not be null");

        val con = Wrappers.lambdaQuery(UserAccountTransitionEntity.class);

        if (!update(transition,
                con.eq(UserAccountTransitionEntity::getId, transition.getId())
                    .eq(UserAccountTransitionEntity::getStatus, status)
                    .eq(UserAccountTransitionEntity::getTenantId, transition.getTenantId())
                    .eq(UserAccountTransitionEntity::getUid, transition.getUid())
                    .eq(UserAccountTransitionEntity::getAsset, transition.getAsset()))) {
            throw new CommonBizException(90011, "无法更新捐款记录");
        }

        return transition;
    }

    @Override
    @Nullable
    public UserAccountTransitionEntity findByIdAndAsset(String tenantId, long id, Asset asset) {
        return lambdaQuery().eq(UserAccountTransitionEntity::getTenantId, tenantId)
            .eq(UserAccountTransitionEntity::getAsset, asset)
            .eq(UserAccountTransitionEntity::getId, id)
            .one();
    }

    @Nullable
    @Override
    public UserAccountTransitionEntity findByIdAndUserIdAndAsset(String tenantId, long id, long uid, Asset asset) {
        return lambdaQuery().eq(UserAccountTransitionEntity::getTenantId, tenantId)
            .eq(UserAccountTransitionEntity::getUid, uid)
            .eq(UserAccountTransitionEntity::getAsset, asset)
            .eq(UserAccountTransitionEntity::getId, id)
            .one();
    }

    @Override
    @Nullable
    public UserAccountTransitionEntity findByUserIdAndBizTypeAndOrderNo(String tenantId, long uid, Asset asset,
            String bizType, String orderNo) {
        return lambdaQuery().eq(UserAccountTransitionEntity::getTenantId, tenantId)
            .eq(UserAccountTransitionEntity::getUid, uid)
            .eq(UserAccountTransitionEntity::getAsset, asset)
            .eq(UserAccountTransitionEntity::getBizType, bizType)
            .eq(UserAccountTransitionEntity::getBizOrderNo, orderNo)
            .one();
    }

    @Override
    public UserAccountTransitionEntity getById(String tenantId, Long id) {
        return lambdaQuery().eq(UserAccountTransitionEntity::getTenantId, tenantId)
            .eq(UserAccountTransitionEntity::getId, id)
            .one();
    }

}

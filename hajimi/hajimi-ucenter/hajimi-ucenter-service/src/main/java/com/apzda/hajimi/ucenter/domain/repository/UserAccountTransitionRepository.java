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

import com.apzda.hajimi.ucenter.domain.entity.UserAccountTransitionEntity;
import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.hajimi.ucenter.domain.enums.TransStatus;
import com.baomidou.mybatisplus.extension.repository.IRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface UserAccountTransitionRepository extends IRepository<UserAccountTransitionEntity> {

    @Nonnull
    UserAccountTransitionEntity updateStatus(@Nonnull UserAccountTransitionEntity transition, TransStatus status);

    @Nullable
    UserAccountTransitionEntity findByIdAndAsset(String tenantId, long id, Asset asset);

    @Nullable
    UserAccountTransitionEntity findByIdAndUserIdAndAsset(String tenantId, long id, long uid, Asset asset);

    @Nullable
    UserAccountTransitionEntity findByUserIdAndBizTypeAndOrderNo(String tenantId, long uid, Asset asset, String bizType,
            String orderNo);

    UserAccountTransitionEntity getById(String tenantId, Long id);

}

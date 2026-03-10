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

import com.apzda.hajimi.ucenter.domain.entity.UserAccountTransactionEntity;
import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountTransactionRepository;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.UserAccountTransactionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
public class UserAccountTransactionRepositoryImpl
        extends ServiceImpl<UserAccountTransactionMapper, UserAccountTransactionEntity>
        implements UserAccountTransactionRepository {

    @Override
    public UserAccountTransactionEntity getById(String tenantId, long transactionId) {
        return lambdaQuery().eq(UserAccountTransactionEntity::getTenantId, tenantId)
            .eq(UserAccountTransactionEntity::getId, transactionId)
            .one();
    }

    @Override
    public UserAccountTransactionEntity getByTransactionHash(String tenantId, String transHash) {
        return lambdaQuery().eq(UserAccountTransactionEntity::getTenantId, tenantId)
            .eq(UserAccountTransactionEntity::getTransHash, transHash)
            .one();
    }

    @Override
    public UserAccountTransactionEntity getLastTransaction(String tenantId, long uid, Asset asset) {
        return lambdaQuery().eq(UserAccountTransactionEntity::getTenantId, tenantId)
            .eq(UserAccountTransactionEntity::getUid, uid)
            .eq(UserAccountTransactionEntity::getAsset, asset)
            .orderByDesc(UserAccountTransactionEntity::getId)
            .last("LIMIT 1")
            .one();
    }

    @Override
    public UserAccountTransactionEntity getByTransitId(String tenantId, Long transitId) {
        return lambdaQuery().eq(UserAccountTransactionEntity::getTenantId, tenantId)
            .eq(UserAccountTransactionEntity::getTransitId, transitId)
            .one();
    }

}

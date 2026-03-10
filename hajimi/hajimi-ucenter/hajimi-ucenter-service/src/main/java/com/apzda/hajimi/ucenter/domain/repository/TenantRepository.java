/*
 * Copyright 2025-2026 the original author or authors.
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

import com.apzda.hajimi.ucenter.controller.admin.dto.TenantPageQuery;
import com.apzda.hajimi.ucenter.controller.admin.dto.TenantUserPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.TenantEntity;
import com.apzda.hajimi.ucenter.domain.entity.TenantSubscriptionEntity;
import com.apzda.hajimi.ucenter.domain.entity.TenantUserEntity;
import com.apzda.hajimi.ucenter.domain.vo.TenantUserVo;
import com.apzda.hajimi.ucenter.service.dto.SubscribeDto;
import com.apzda.hajimi.ucenter.service.dto.TenantDto;
import com.apzda.hajimi.ucenter.tenant.DefaultSubscription;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.service.SerialNumberGenerator;
import com.apzda.kalami.tenant.Tenant;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.repository.IRepository;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface TenantRepository extends IRepository<TenantEntity> {

    TenantEntity findByCode(String code);

    IPage<TenantEntity> pageQuery(TenantPageQuery query, QuickSearch quickSearch);

    boolean syncPrivileges(TenantEntity tenant);

    TenantUserEntity join(Long id, Long uid);

    boolean leave(Long id, Long uid);

    TenantUserEntity findUserByTenantAndUid(Long tenantId, Long uid);

    TenantEntity create(TenantDto dto);

    TenantEntity update(TenantDto dto);

    IPage<TenantUserVo> users(TenantUserPageQuery query, QuickSearch quickSearch);

    List<TenantUserVo> listTenantByUid(String uid);

    List<DefaultSubscription> listSubscriptionsById(String tenantId);

    List<TenantSubscriptionEntity> findSubscriptionsById(String tenantId);

    boolean switchTo(Long id, Long uid);

    List<Tenant> listChildren(String parentId);

    DefaultSubscription subscribe(SubscribeDto dto);

    void unsubscribe(String id, String service);

    boolean phoneExist(String phone);

}

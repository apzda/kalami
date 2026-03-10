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

package com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper;

import com.apzda.hajimi.ucenter.domain.entity.TenantSubscriptionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface TenantSubscriptionMapper extends BaseMapper<TenantSubscriptionEntity> {

    @Select("SELECT * FROM ucenter_tenant_subscription WHERE tenant_id = #{tenantId} AND service = #{service}")
    TenantSubscriptionEntity getByTenantAndService(@Param("tenantId") String tenantId,
            @Param("service") String service);

    @Update("UPDATE ucenter_tenant_subscription SET deleted = 0 WHERE tenant_id = #{tenantId} AND service = #{service}")
    void subscribe(@Param("tenantId") String tenantId, @Param("service") String service);

    @Update("UPDATE ucenter_tenant_subscription SET deleted = 1 WHERE tenant_id = #{tenantId} AND service = #{service}")
    void unsubscribe(@Param("tenantId") String tenantId, @Param("service") String service);

}

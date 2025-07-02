/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.data.jpa;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.SystemClock;
import com.apzda.kalami.data.domain.Auditable;
import com.apzda.kalami.data.domain.TenantAware;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUserProvider;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ResolvableType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public class AuditingEntityListener {

    @PrePersist
    @PreUpdate
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void fillMetaData(Object o) {
        if (o instanceof Auditable entity) {
            val userId = CurrentUserProvider.getCurrentUser().getUid();

            val resolvableType = ResolvableType.forClass(Auditable.class, o.getClass());

            val userIdClz = resolvableType.getGeneric(0).resolve();

            Serializable uid;
            if (userIdClz == null) {
                uid = null;
            }
            else if (Long.class.isAssignableFrom(userIdClz)) {
                uid = Long.parseLong(userId);
            }
            else if (Integer.class.isAssignableFrom(userIdClz)) {
                uid = Integer.parseInt(userId);
            }
            else if (StringUtils.isNotBlank(userId)) {
                uid = userId;
            }
            else {
                uid = null;
            }

            if (uid != null) {
                if (entity.getCreatedBy() == null) {
                    entity.setCreatedBy(uid);
                }
                entity.setUpdatedBy(uid);
            }
            val timeType = resolvableType.getGeneric(1).resolve();

            Serializable current;
            if (timeType == null || Long.class.isAssignableFrom(timeType)) {
                current = SystemClock.now();
            }
            else if (Date.class.isAssignableFrom(timeType)) {
                current = new Date();
            }
            else if (LocalDate.class.isAssignableFrom(timeType)) {
                current = LocalDate.now();
            }
            else if (LocalDateTime.class.isAssignableFrom(timeType)) {
                current = LocalDateTime.now().withNano(0);
            }
            else if (Integer.class.isAssignableFrom(timeType)) {
                current = DateUtil.currentSeconds();
            }
            else {
                current = DateUtil.now();
            }

            if (entity.getCreatedDate() == null) {
                entity.setCreatedDate(current);
            }
            entity.setUpdatedDate(current);
        }

        if (o instanceof TenantAware tenantAware) {
            val tenantId = TenantManager.tenantId();
            if (Objects.isNull(tenantAware.getTenantId()) && tenantId != null) {
                val resolvableType = ResolvableType.forClass(TenantAware.class, o.getClass());
                val aClass = resolvableType.getGeneric(0).resolve();
                Serializable tid;
                if (aClass == null) {
                    tid = null;
                }
                else if (Long.class.isAssignableFrom(aClass)) {
                    tid = Long.parseLong(tenantId);
                }
                else if (Integer.class.isAssignableFrom(aClass)) {
                    tid = Integer.parseInt(tenantId);
                }
                else if (StringUtils.isNotBlank(tenantId)) {
                    tid = tenantId;
                }
                else {
                    tid = null;
                }
                tenantAware.setTenantId(tid);
            }
        }
    }

}

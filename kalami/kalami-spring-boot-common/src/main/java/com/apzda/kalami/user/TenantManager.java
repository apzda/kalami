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
package com.apzda.kalami.user;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public abstract class TenantManager<T> implements InitializingBean {

    private static final Object[] SYS_TENANT_IDS = new Object[] { null };

    private static TenantManager<?> tenantManager;

    private static Class<?> fieldType;

    protected String tenantIdColumn = "tenant_id";

    protected Boolean disableTenantPlugin = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        tenantManager = this;
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> T[] tenantIds() {
        if (tenantManager != null) {
            val tenantIds = tenantManager.getTenantIds();
            if (tenantIds.length > 0) {
                return (T[]) tenantIds;
            }
        }
        return (T[]) SYS_TENANT_IDS;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T tenantId() {
        if (Objects.isNull(tenantIds()[0])) {
            return null;
        }
        return (T) tenantIds()[0];
    }

    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> T tenantId(@Nonnull T defaultTenantId) {
        Assert.notNull(defaultTenantId, "Default tenant ID cannot be null");
        if (Objects.isNull(tenantIds()[0])) {
            return defaultTenantId;
        }
        return (T) tenantIds()[0];
    }

    @Nonnull
    public String getTenantIdColumn() {
        return StringUtils.defaultIfBlank(tenantIdColumn, "tenant_id");
    }

    public boolean disableTenantPlugin() {
        return !Boolean.FALSE.equals(this.disableTenantPlugin);
    }

    @Nonnull
    protected abstract T[] getTenantIds();

    public static Class<?> getIdType() {
        if (fieldType == null && tenantManager != null) {
            fieldType = ResolvableType.forClass(TenantManager.class, tenantManager.getClass()).getGeneric(0).resolve();
        }

        return fieldType;
    }

}

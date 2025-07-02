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
package com.apzda.kalami.tenant;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Slf4j
public abstract class TenantManager implements InitializingBean {

    private static final String[] SYS_TENANT_IDS = new String[] { null };

    private static final String[] SYS_ORG_IDS = new String[] { null };

    private static TenantManager tenantManager;

    protected String tenantIdColumn = "tenant_id";

    protected Boolean disableTenantPlugin = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        tenantManager = this;
    }

    public static void resetTenantManager() {
        if (tenantManager != null) {
            tenantManager.reset();
        }
    }

    @Nonnull
    public static String[] tenantIds() {
        if (tenantManager != null) {
            val tenantIds = tenantManager.getTenantIds();
            if (tenantIds != null && tenantIds.length > 0) {
                return tenantIds;
            }
        }
        return SYS_TENANT_IDS;
    }

    @Nonnull
    public static String[] organizationIds() {
        if (tenantManager != null) {
            val orgIds = tenantManager.getOrganizationIds(tenantId());
            if (orgIds != null && orgIds.length > 0) {
                return orgIds;
            }
        }

        return SYS_ORG_IDS;
    }

    @Nullable
    public static String tenantId() {
        return tenantIds()[0];
    }

    @Nullable
    public static String currentTenantId() {
        return tenantIds()[0];
    }

    @Nonnull
    public static String tenantId(@Nonnull String defaultTenantId) {
        Assert.notNull(defaultTenantId, "Default tenant ID cannot be null");
        val tenantId = tenantIds()[0];
        if (tenantId == null) {
            return defaultTenantId;
        }

        return tenantId;
    }

    @Nonnull
    public static String currentTenantId(@Nonnull String defaultTenantId) {
        return tenantId(defaultTenantId);
    }

    @Nullable
    public static String orgId() {
        return organizationIds()[0];
    }

    @Nullable
    public static String currentOrgId() {
        return organizationIds()[0];
    }

    @Nonnull
    public static String orgId(String defaultOrgId) {
        Assert.notNull(defaultOrgId, "Default Org ID cannot be null");
        val id = organizationIds()[0];
        if (id == null) {
            return defaultOrgId;
        }

        return id;
    }

    @Nonnull
    public static String currentOrgId(String defaultOrgId) {
        return orgId(defaultOrgId);
    }

    @Nonnull
    public static Map<String, Subscription> subscriptions() {
        if (tenantManager == null) {
            return Collections.emptyMap();
        }
        try {
            val subscriptions = tenantManager.getSubscriptions(tenantId());
            return Objects.requireNonNullElse(subscriptions, Collections.emptyMap());
        }
        catch (Exception ex) {
            log.warn("Failed to get subscriptions for tenant {} - {}", tenantId(), ex.getMessage());
            return Collections.emptyMap();
        }
    }

    @Nonnull
    public static Map<String, Subscription> availableSubscriptions() {
        if (tenantManager == null) {
            return Collections.emptyMap();
        }
        val subscriptions = tenantManager.getAvailableSubscriptions();
        if (subscriptions == null) {
            return Collections.emptyMap();
        }
        return subscriptions;
    }

    @Nonnull
    public String getTenantIdColumn() {
        return StringUtils.defaultIfBlank(tenantIdColumn, "tenant_id");
    }

    public boolean disableTenantPlugin() {
        return !Boolean.FALSE.equals(this.disableTenantPlugin);
    }

    protected abstract String[] getTenantIds();

    protected String[] getOrganizationIds(String tenantId) {
        return SYS_ORG_IDS;
    }

    protected void reset() {
    }

    protected Map<String, Subscription> getSubscriptions(String tenantId) {
        return Collections.emptyMap();
    }

    protected Map<String, Subscription> getAvailableSubscriptions() {
        return Collections.emptyMap();
    }

}

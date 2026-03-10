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
package com.apzda.hajimi.ucenter.security;

import com.apzda.hajimi.ucenter.config.UCenterTenantConfigureProperties;
import com.apzda.hajimi.ucenter.service.UCenterService;
import com.apzda.hajimi.ucenter.tenant.DefaultSubscription;
import com.apzda.kalami.tenant.Organization;
import com.apzda.kalami.tenant.Subscription;
import com.apzda.kalami.tenant.Tenant;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Lazy;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class UCenterTenantManager extends TenantManager {

    private static final String[] DEFAULT = new String[] { "-1" };

    private static final String[] DEFAULT_ORG = new String[] { null };

    private static final ThreadLocal<Map<String, String[]>> IDS = new InheritableThreadLocal<>();

    private static final ThreadLocal<Map<String, String[]>> ORG = new InheritableThreadLocal<>();

    private final Lazy<UCenterService> uCenterServiceLazy;

    private final UCenterTenantConfigureProperties properties;

    @Override
    protected String[] getTenantIds() {
        val user = CurrentUserProvider.getCurrentUser();
        val uid = user.getUid();
        if (StringUtils.isBlank(uid)) {
            return DEFAULT;
        }
        Map<String, String[]> cache = IDS.get();
        if (cache == null) {
            cache = new HashMap<>();
            IDS.set(cache);
        }
        if (cache.containsKey(uid)) {
            return cache.get(uid);
        }
        try {
            val tenants = uCenterServiceLazy.get().getTenants(uid);
            if (tenants.isEmpty()) {
                cache.put(uid, DEFAULT);
                return DEFAULT;
            }
            val ids = tenants.stream().map(Tenant::getId).toArray(String[]::new);
            cache.put(uid, ids);
            return ids;
        }
        catch (Exception e) {
            log.warn("Cannot get tenants for '{}' - {}", uid, e.getMessage());
            cache.put(uid, DEFAULT);
            return DEFAULT;
        }
    }

    @Override
    protected String[] getOrganizationIds(String tenantId) {
        val user = CurrentUserProvider.getCurrentUser();
        val uid = user.getUid();
        val key = uid + "@" + tenantId;
        Map<String, String[]> cache = ORG.get();
        if (cache == null) {
            cache = new HashMap<>();
            ORG.set(cache);
        }
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        try {
            List<Organization> organizations = uCenterServiceLazy.get().getOrganizations(tenantId, uid);
            if (organizations.isEmpty()) {
                cache.put(key, DEFAULT_ORG);
                return DEFAULT_ORG;
            }
            val ids = organizations.stream().map(Organization::getId).toArray(String[]::new);
            cache.put(key, ids);
            return ids;
        }
        catch (Exception e) {
            log.warn("Cannot get organizations for '{}' - {}", key, e.getMessage());
            cache.put(key, DEFAULT);
            return DEFAULT;
        }
    }

    @Override
    protected void reset() {
        IDS.remove();
        ORG.remove();
    }

    @Override
    protected Map<String, Subscription> getSubscriptions(String tenantId) {
        val subs = new HashMap<String, Subscription>();
        val defaultSubscription = getAvailableSubscriptions(false).get("core");
        if (defaultSubscription != null) {
            subs.put(defaultSubscription.getId(), defaultSubscription);
        }

        try {
            List<DefaultSubscription> subscriptions = uCenterServiceLazy.get().getSubscriptions(tenantId);
            if (!CollectionUtils.isEmpty(subscriptions)) {
                subscriptions.forEach(subscription -> subs.put(subscription.getId(), subscription));
            }
        }
        catch (Exception ignored) {
        }

        return subs;
    }

    @Override
    protected Map<String, Subscription> getAvailableSubscriptions(boolean all) {
        val service = properties.getService();
        if (CollectionUtils.isEmpty(service)) {
            return Collections.emptyMap();
        }

        Map<String, Subscription> subscriptions = new HashMap<>();

        service.forEach((id, svc) -> {
            if (all || svc.isLeasable()) {
                val sub = new DefaultSubscription();
                sub.setId(id);
                sub.setName(svc.getName());
                sub.setLogo(svc.getLogo());
                sub.setDescription(svc.getDescription());

                subscriptions.put(id, sub);
            }
        });

        return subscriptions;
    }

}

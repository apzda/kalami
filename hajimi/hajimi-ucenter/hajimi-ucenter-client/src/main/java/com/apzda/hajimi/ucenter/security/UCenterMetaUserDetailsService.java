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

import com.apzda.hajimi.ucenter.service.UCenterService;
import com.apzda.kalami.security.user.MetaUserDetailsService;
import com.apzda.kalami.tenant.TenantManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public class UCenterMetaUserDetailsService implements MetaUserDetailsService {

    private final LoadingCache<String, List<SimpleGrantedAuthority>> cache = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(3600))
        .build(new CacheLoader<>() {
            @Override
            @Nonnull
            public List<SimpleGrantedAuthority> load(@Nonnull String key) throws Exception {
                try {
                    return ucenterServiceLoader.get().getAuthority(key);
                }
                catch (Exception e) {
                    log.warn("Loading user({})'s authorities failed: {}", key, e.getMessage());
                    return List.of();
                }
            }
        });

    private final Lazy<UCenterService> ucenterServiceLoader;

    public UCenterMetaUserDetailsService(ApplicationContext applicationContext) {
        this.ucenterServiceLoader = Lazy.of(() -> applicationContext.getBean(UCenterService.class));
    }

    @Override
    public @Nonnull Collection<? extends GrantedAuthority> getAuthorities(@Nonnull UserDetails userDetails,
            Authentication authentication) {
        val key = genKey(userDetails);
        try {
            return cache.get(key);
        }
        catch (ExecutionException e) {
            log.warn("Getting user({})'s authorities failed: {}", key, e.getMessage());
            return List.of();
        }
    }

    @Override
    public void clearCache(String tenantId, String orgId, String username) {
        val tid = Optional.ofNullable(tenantId).orElse("0");
        val oid = Optional.ofNullable(orgId).orElse("");
        val key = String.format("%s@%s@%s", username, tid, oid);
        log.debug("Clearing cache for user's session: {}", key);

        try {
            cache.invalidate(key);
        }
        catch (Exception ignored) {
        }

        try {
            ucenterServiceLoader.get().clearUserCache(tid, oid, username);
        }
        catch (Exception e) {
            log.warn("Clearing user({})'s authorities failed: {}", key, e.getMessage());
        }

        log.debug("Cache for user's session cleared: {}", key);
    }

    @Nonnull
    private String genKey(@Nonnull UserDetails userDetails) {
        val userId = userDetails.getUsername();
        val tenantId = TenantManager.tenantId("0");
        val orgId = Optional.ofNullable(TenantManager.orgId()).orElse("");

        return userId + "@" + tenantId + "@" + orgId;
    }

}

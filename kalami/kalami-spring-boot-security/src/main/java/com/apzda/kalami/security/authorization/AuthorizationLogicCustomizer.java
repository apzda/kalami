/*
 * Copyright 2025 the original author or authors.
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

package com.apzda.kalami.security.authorization;

import com.apzda.kalami.data.domain.OwnerAware;
import com.apzda.kalami.data.domain.TenantAware;
import com.apzda.kalami.security.utils.SecurityUtils;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUserProvider;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class AuthorizationLogicCustomizer {

    private final PermissionEvaluator evaluator;

    public boolean isSa() {
        return SecurityUtils.hasRole("sa");
    }

    public boolean is(String role) {
        return SecurityUtils.hasRole(role);
    }

    public boolean iCan(String authority) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAuthed() && evaluator.hasPermission(authentication, null, authority);
    }

    public boolean iCan(String authority, Object object) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAuthed() && evaluator.hasPermission(authentication, object, authority);
    }

    public boolean has(String authority) {
        if (StringUtils.isBlank(authority)) {
            return false;
        }
        if (Strings.CS.startsWith(authority, "!")) {
            return isAuthed() && !SecurityUtils.hasAuthority(authority.substring(1));
        }
        return isAuthed() && SecurityUtils.hasAuthority(authority);
    }

    public boolean sysUser() {
        return has("!TENANT");
    }

    public boolean isMine(@Nullable Object owner) {
        if (!isAuthed() || owner == null) {
            return false;
        }
        if (owner instanceof String id) {
            val me = CurrentUserProvider.getCurrentUser();
            val uid = me.getUid();
            return Objects.equals(uid, id);
        }
        else if (owner instanceof OwnerAware<?> ownerAware) {
            val me = CurrentUserProvider.getCurrentUser();
            val uid = me.getUid();
            return Objects.equals(uid, ownerAware.getCreatedBy().toString());
        }
        else if (owner instanceof TenantAware<?> tenantAware) {
            val tenantId = tenantAware.getTenantId();
            return Arrays.stream(TenantManager.tenantIds())
                .filter(Objects::nonNull)
                .map(Object::toString)
                .anyMatch((id) -> Objects.equals(id, tenantId));
        }

        return false;
    }

    public boolean isTenanted(@Nullable TenantAware<?> object) {
        if (!isAuthed() || object == null || object.getTenantId() == null) {
            return false;
        }

        val tenantId = object.getTenantId().toString();
        return Arrays.stream(TenantManager.tenantIds())
            .filter(Objects::nonNull)
            .map(Object::toString)
            .anyMatch((id) -> id.equals(tenantId));
    }

    public boolean isTenanted(@Nullable String tenantId) {
        if (!isAuthed() || StringUtils.isBlank(tenantId)) {
            return false;
        }

        return Arrays.stream(TenantManager.tenantIds())
            .filter(Objects::nonNull)
            .map(Object::toString)
            .anyMatch((id) -> id.equals(tenantId));
    }

    public boolean subscribed(@Nonnull String service) {
        if (!isAuthed()) {
            return false;
        }

        if (StringUtils.isBlank(service)) {
            return true;
        }

        val subscriptions = TenantManager.subscriptions();

        if (CollectionUtils.isEmpty(subscriptions)) {
            throw new AccessDeniedException("未订阅任何服务");
        }

        val subscription = subscriptions.get(service);
        if (subscription == null) {
            throw new AccessDeniedException(String.format("【%s】服务未订阅", service));
        }
        if (subscription.getExpireTime() != null && subscription.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new AccessDeniedException(String.format("【%s】服务订阅已过期", service));
        }
        return true;
    }

    public boolean isAuthed() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "An Authentication object was not found in the SecurityContext");
        }
        return authentication.isAuthenticated();
    }

}

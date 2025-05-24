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
import com.apzda.kalami.user.CurrentUserProvider;
import com.apzda.kalami.user.TenantManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

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
        return SecurityUtils.hasRole("admin");
    }

    public boolean iCan(String authority) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAuthed() && evaluator.hasPermission(authentication, null, authority);
    }

    public boolean iCan(String authority, Object object) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        return isAuthed() && evaluator.hasPermission(authentication, object, authority);
    }

    public boolean isMine(@Nullable OwnerAware<?> object) {
        if (!isAuthed() || object == null || object.getCreatedBy() == null) {
            return false;
        }
        val me = CurrentUserProvider.getCurrentUser();
        val uid = me.getUid();
        val owner = object.getCreatedBy().toString();

        return Objects.equals(uid, owner);
    }

    public boolean isMine(@Nullable String owner) {
        if (!isAuthed() || StringUtils.isBlank(owner)) {
            return false;
        }
        val me = CurrentUserProvider.getCurrentUser();
        val uid = me.getUid();
        return Objects.equals(uid, owner);
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

    public boolean isAuthed() {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException(
                    "An Authentication object was not found in the SecurityContext");
        }
        return authentication.isAuthenticated();
    }

}

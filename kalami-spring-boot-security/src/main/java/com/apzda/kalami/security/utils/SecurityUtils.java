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

package com.apzda.kalami.security.utils;

import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.error.AuthenticationError;
import com.apzda.kalami.security.token.JwtToken;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Slf4j
public abstract class SecurityUtils {

    public static final String CONTEXT_ATTR_NAME = "GSVC.SECURITY.CONTEXT";

    public static final String CONTEXT_ATTR_EXCEPTION = "GSVC.SECURITY.EXCEPTION";

    private static DefaultSecurityExpressionHandler handler;

    @Nonnull
    public static SecurityExpressionRoot user() {
        return handler.create();
    }

    @Nonnull
    public static JwtTokenAuthentication getAuthentication() {
        if (handler.create().getAuthentication() instanceof JwtTokenAuthentication token) {
            return token;
        }
        throw new AccessDeniedException("Current authentication is not a JwtAuthenticationToken instance");
    }

    @Nonnull
    public static JwtToken getCurrentToken() {
        return getAuthentication().getJwtToken();
    }

    public static boolean hasAuthority(String... authority) {
        if (authority == null || authority.length == 0) {
            return false;
        }
        val root = handler.create();

        return Arrays.stream(authority).map(root::hasAuthority).filter(e -> !e).findFirst().isEmpty();
    }

    public static boolean hasAnyAuthority(String... authority) {
        val root = handler.create();
        return root.hasAnyAuthority(authority);
    }

    public static boolean hasRole(String... role) {
        if (role == null || role.length == 0) {
            return false;
        }
        val root = handler.create();

        return Arrays.stream(role).map(root::hasRole).filter(e -> !e).findFirst().isEmpty();
    }

    public static boolean hasAnyRole(String... role) {
        val root = handler.create();
        return root.hasAnyRole(role);
    }

    public static boolean hasPermission(@Nonnull String permission) {
        val root = handler.create();
        return root.hasPermission(null, permission);
    }

    public static boolean hasPermission(@Nonnull Object target, @Nonnull String permission) {
        val root = handler.create();
        return root.hasPermission(target, permission);
    }

    @Nonnull
    public static UserDetails checkUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UsernameNotFoundException("");
        }

        val username = userDetails.getUsername();
        if (StringUtils.isBlank(username)) {
            throw new UsernameNotFoundException("username is blank");
        }

        if (!userDetails.isEnabled()) {
            throw new DisabledException(String.format("%s Disabled", username));
        }

        if (!userDetails.isAccountNonExpired()) {
            throw new AccountExpiredException(String.format("%s's Account Expired", username));
        }

        return userDetails;
    }

    @Nullable
    public static String fromBearerToken(String headerName, String bearer, @Nonnull String token) {
        String accessToken;
        if (StringUtils.isNotBlank(bearer) && token.startsWith(bearer)) {
            accessToken = token.substring(bearer.length() + 1);
            if (log.isTraceEnabled()) {
                log.trace("Try to get token from header({}) with bearer({}): {}", headerName, bearer, accessToken);
            }
        }
        else if (StringUtils.isBlank(bearer)) {
            accessToken = token;
            if (log.isTraceEnabled()) {
                log.trace("Try to get token from header({}): {}", headerName, accessToken);
            }
        }
        else {
            accessToken = null;
        }

        return accessToken;
    }

    static class DefaultSecurityExpressionRoot extends SecurityExpressionRoot {

        public DefaultSecurityExpressionRoot(Supplier<Authentication> authentication) {
            super(authentication);
        }

    }

    public static class DefaultSecurityExpressionHandler {

        public static final ThreadLocal<SecurityExpressionRoot> ROOT_BOX = new ThreadLocal<>();

        private final PermissionEvaluator permissionEvaluator;

        private final RoleHierarchy roleHierarchy;

        private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

        private final String rolePrefix;

        public DefaultSecurityExpressionHandler(PermissionEvaluator permissionEvaluator, RoleHierarchy roleHierarchy,
                String rolePrefix) {
            this.permissionEvaluator = permissionEvaluator;
            this.roleHierarchy = roleHierarchy;
            this.rolePrefix = rolePrefix;
            SecurityUtils.handler = this;
        }

        SecurityExpressionRoot create() {
            SecurityExpressionRoot root = ROOT_BOX.get();
            if (root != null) {
                return root;
            }
            synchronized (this) {
                root = ROOT_BOX.get();
                if (root != null) {
                    return root;
                }
                val authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null) {
                    throw new AuthenticationCredentialsNotFoundException(
                            "An Authentication object was not found in the SecurityContext");
                }

                if (!authentication.isAuthenticated()) {
                    throw new AuthenticationError(ServiceError.UNAUTHORIZED);
                }

                root = new DefaultSecurityExpressionRoot(() -> authentication);
                root.setDefaultRolePrefix(rolePrefix);
                root.setPermissionEvaluator(permissionEvaluator);
                root.setRoleHierarchy(roleHierarchy);
                root.setTrustResolver(trustResolver);
                return root;
            }
        }

    }

}

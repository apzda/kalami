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

package com.apzda.kalami.security.authentication.repository;

import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.security.authentication.DeviceAuthenticationDetails;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.error.AuthenticationError;
import com.apzda.kalami.security.token.TokenManager;
import com.apzda.kalami.security.web.filter.KalamiWebSecurityContextFilter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import static com.apzda.kalami.security.utils.SecurityUtils.CONTEXT_ATTR_EXCEPTION;
import static com.apzda.kalami.security.utils.SecurityUtils.CONTEXT_ATTR_NAME;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Slf4j
public class JwtContextRepository implements SecurityContextRepository {

    private static final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy();

    private final TokenManager tokenManager;

    private final SecurityConfigProperties properties;

    public JwtContextRepository(TokenManager tokenManager, SecurityConfigProperties properties) {
        this.tokenManager = tokenManager;
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("deprecation")
    public SecurityContext loadContext(@NonNull HttpRequestResponseHolder requestResponseHolder) {
        val request = requestResponseHolder.getRequest();
        log.trace("Start loading SecurityContext");

        val storedContext = request.getAttribute(CONTEXT_ATTR_NAME);
        if (storedContext != null) {
            log.trace("Context Loaded from request attribute");
            return (SecurityContext) storedContext;
        }

        val context = securityContextHolderStrategy.createEmptyContext();

        try {
            val authentication = restoreAuthentication(request);

            if (authentication != null) {
                context.setAuthentication(authentication);
                log.trace("Context loaded from TokenManager: {}", tokenManager.getClass().getName());
            }
        }
        catch (AuthenticationError error) {
            request.setAttribute(CONTEXT_ATTR_EXCEPTION, error);
            val authentication = error.getAuthentication();
            if (authentication != null) {
                context.setAuthentication(authentication);
                log.trace("Context loaded from TokenManager with an exception: {} - {}",
                        tokenManager.getClass().getName(), error.getMessage());
            }
        }
        catch (AuthenticationException authenticationException) {
            request.setAttribute(CONTEXT_ATTR_EXCEPTION, authenticationException);
        }
        catch (Exception e) {
            log.error("Error happened while loading Context: {}", e.getMessage(), e);
        }

        securityContextHolderStrategy.setContext(context);
        request.setAttribute(CONTEXT_ATTR_NAME, context);

        if (log.isTraceEnabled()) {
            log.trace("End loading SecurityContext: {}", context);
        }
        return context;
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        try {
            tokenManager.save(context.getAuthentication());
            if (log.isTraceEnabled()) {
                log.trace("SecurityContext saved: {}", context);
            }
        }
        catch (Exception e) {
            log.error("Save Context failed: {} - {}", e.getMessage(), context);
        }
    }

    @Override
    public boolean containsContext(@Nonnull HttpServletRequest request) {
        return request.getAttribute(CONTEXT_ATTR_NAME) != null;
    }

    @Nullable
    private Authentication restoreAuthentication(@Nonnull HttpServletRequest request) {
        val accessToken = KalamiWebSecurityContextFilter.getAccessTokenFromRequest(request, properties);

        if (StringUtils.isNotBlank(accessToken)) {
            AuthenticationError exception = null;
            Authentication authentication;
            try {
                authentication = tokenManager.restore(accessToken);
            }
            catch (AuthenticationError error) {
                authentication = error.getAuthentication();
                exception = error;
            }
            if (authentication instanceof AbstractAuthenticationToken jwtAuthenticationToken) {
                jwtAuthenticationToken.setDetails(DeviceAuthenticationDetails.create(KalamiContextHolder.headers(),
                        KalamiContextHolder.getRemoteIp()));
            }
            if (exception != null) {
                throw exception.withAuthentication(authentication);
            }
            return authentication;
        }

        log.trace("No JWT token found");
        return null;
    }

}

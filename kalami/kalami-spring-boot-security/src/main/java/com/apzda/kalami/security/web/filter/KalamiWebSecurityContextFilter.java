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

package com.apzda.kalami.security.web.filter;

import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.security.authentication.DeviceAuthenticationDetails;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.error.AuthenticationError;
import com.apzda.kalami.security.token.TokenManager;
import com.apzda.kalami.security.utils.SecurityUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.apzda.kalami.security.utils.SecurityUtils.CONTEXT_ATTR_EXCEPTION;
import static com.apzda.kalami.security.utils.SecurityUtils.CONTEXT_ATTR_NAME;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/21
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class KalamiWebSecurityContextFilter extends OncePerRequestFilter {

    private static final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy();

    private final SecurityConfigProperties properties;

    private final TokenManager tokenManager;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {
        val storedContext = request.getAttribute(CONTEXT_ATTR_NAME);
        if (storedContext != null) {
            log.trace("Context Loaded from request attribute");
            filterChain.doFilter(request, response);

            return;
        }

        val context = securityContextHolderStrategy.createEmptyContext();
        try {
            val accessToken = getAccessTokenFromRequest(request, properties);
            if (StringUtils.isNotBlank(accessToken)) {
                val authentication = tokenManager.restore(accessToken);

                if (authentication != null) {
                    if (authentication instanceof AbstractAuthenticationToken jwtAuthenticationToken) {
                        val headers = KalamiContextHolder.headers();
                        val remoteAddr = KalamiContextHolder.getRemoteAddr();
                        jwtAuthenticationToken.setDetails(DeviceAuthenticationDetails.create(headers, remoteAddr));
                    }
                    context.setAuthentication(authentication);
                }
            }
        }
        catch (AuthenticationError error) {
            request.setAttribute(CONTEXT_ATTR_EXCEPTION, error);
            val authentication = error.getAuthentication();

            if (authentication != null) {
                if (authentication instanceof AbstractAuthenticationToken jwtAuthenticationToken) {
                    val headers = KalamiContextHolder.headers();
                    val remoteAddr = KalamiContextHolder.getRemoteAddr();
                    jwtAuthenticationToken.setDetails(DeviceAuthenticationDetails.create(headers, remoteAddr));
                }
                context.setAuthentication(authentication);
            }
        }
        catch (AuthenticationException authenticationException) {
            request.setAttribute(CONTEXT_ATTR_EXCEPTION, authenticationException);
        }
        catch (Exception e) {
            log.error("Error happened while loading Context: {}", e.getMessage());
        }

        securityContextHolderStrategy.setContext(context);
        request.setAttribute(CONTEXT_ATTR_NAME, context);

        if (log.isTraceEnabled()) {
            log.trace("End loading SecurityContext: {}", context);
        }

        filterChain.doFilter(request, response);
    }

    @Nullable
    public static String getAccessTokenFromRequest(@Nonnull HttpServletRequest request,
            @Nonnull SecurityConfigProperties properties) {
        val argName = properties.getArgName();
        val headerName = properties.getTokenName();
        val cookieConfig = properties.getCookie();
        val cookieName = cookieConfig.getCookieName();
        val bearer = properties.getBearer();

        String accessToken = null;
        if (StringUtils.isNotBlank(argName)) {
            accessToken = StringUtils.defaultIfBlank(request.getParameter(argName), null);
            if (log.isTraceEnabled()) {
                log.trace("Try to get token from parameter({}): {}", argName, accessToken);
            }
        }

        val token = request.getHeader(headerName);
        if (StringUtils.isBlank(accessToken) && StringUtils.isNotBlank(token)) {
            accessToken = SecurityUtils.fromBearerToken(headerName, bearer, token);
        }

        if (StringUtils.isBlank(accessToken) && StringUtils.isNotBlank(cookieName)) {
            accessToken = KalamiContextHolder.cookies().get(cookieName).getValue();
            if (log.isTraceEnabled()) {
                log.trace("Try to get token from cookie({}): {}", cookieName, accessToken);
            }
        }

        return accessToken;
    }

}

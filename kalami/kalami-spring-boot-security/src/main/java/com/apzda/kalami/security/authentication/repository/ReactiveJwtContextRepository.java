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

import com.apzda.kalami.security.authentication.DeviceAuthenticationDetails;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.error.AuthenticationError;
import com.apzda.kalami.security.token.TokenManager;
import com.apzda.kalami.security.utils.SecurityUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.apzda.kalami.security.utils.SecurityUtils.CONTEXT_ATTR_EXCEPTION;
import static com.apzda.kalami.security.utils.SecurityUtils.CONTEXT_ATTR_NAME;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ReactiveJwtContextRepository implements ServerSecurityContextRepository {

    private static final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy();

    private final TokenManager tokenManager;

    private final SecurityConfigProperties properties;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // tokenManager.save(context.getAuthentication());
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(@Nonnull ServerWebExchange exchange) {
        val request = exchange.getRequest();
        log.trace("Start loading SecurityContext");

        val storedContext = request.getAttributes().get(CONTEXT_ATTR_NAME);
        if (storedContext != null) {
            log.trace("Context Loaded from request attribute");
            return Mono.just((SecurityContext) storedContext);
        }

        val context = securityContextHolderStrategy.createEmptyContext();

        try {
            val authentication = restoreAuthentication(request);

            if (authentication != null) {
                context.setAuthentication(authentication);
                log.trace("Context loaded from TokenManager: {}", tokenManager);
            }
        }
        catch (AuthenticationError error) {
            request.getAttributes().put(CONTEXT_ATTR_EXCEPTION, error);
            val authentication = error.getAuthentication();
            if (authentication instanceof AbstractAuthenticationToken jwtAuthenticationToken) {
                val headers = request.getHeaders();
                var ip = "127.0.0.1";
                if (Optional.ofNullable(request.getRemoteAddress()).isPresent()) {
                    ip = request.getRemoteAddress().getAddress().getHostAddress();
                }
                jwtAuthenticationToken.setDetails(DeviceAuthenticationDetails.create(headers, ip));
            }

            if (authentication != null) {
                context.setAuthentication(authentication);
                if (log.isTraceEnabled()) {
                    log.trace("Context loaded from TokenManager with an exception: {} - {}", tokenManager,
                            error.getMessage());
                }
            }
        }
        catch (AuthenticationException authenticationException) {
            request.getAttributes().put(CONTEXT_ATTR_EXCEPTION, authenticationException);
        }
        catch (Exception e) {
            log.error("Error happened while loading Context: {}", e.getMessage());
        }

        securityContextHolderStrategy.setContext(context);
        request.getAttributes().put(CONTEXT_ATTR_NAME, context);

        if (log.isTraceEnabled()) {
            log.trace("End loading SecurityContext: {}", context);
        }
        return Mono.just(context);
    }

    @Nullable
    private Authentication restoreAuthentication(@Nonnull ServerHttpRequest request) {
        val accessToken = getAccessTokenFromRequest(request, properties);

        if (StringUtils.isNotBlank(accessToken)) {
            val authentication = tokenManager.restore(accessToken);
            if (authentication instanceof AbstractAuthenticationToken jwtAuthenticationToken) {
                val headers = request.getHeaders();
                var ip = "127.0.0.1";
                if (Optional.ofNullable(request.getRemoteAddress()).isPresent()) {
                    ip = request.getRemoteAddress().getAddress().getHostAddress();
                }
                jwtAuthenticationToken.setDetails(DeviceAuthenticationDetails.create(headers, ip));
            }
            return authentication;
        }

        log.trace("No JWT token found");
        return null;
    }

    private String getAccessTokenFromRequest(@Nonnull ServerHttpRequest request,
            @Nonnull SecurityConfigProperties properties) {
        val argName = properties.getArgName();
        val headerName = properties.getTokenName();
        val cookieConfig = properties.getCookie();
        val cookieName = cookieConfig.getCookieName();
        val bearer = properties.getBearer();

        String accessToken = null;
        if (StringUtils.isNotBlank(argName)) {

            accessToken = StringUtils.defaultIfBlank(request.getQueryParams().getFirst(argName), null);
            if (log.isTraceEnabled()) {
                log.trace("Try to get token from parameter({}): {}", argName, accessToken);
            }
        }

        val token = request.getHeaders().getFirst(headerName);
        if (StringUtils.isBlank(accessToken) && StringUtils.isNotBlank(token)) {
            accessToken = SecurityUtils.fromBearerToken(headerName, bearer, token);
        }

        if (StringUtils.isBlank(accessToken) && StringUtils.isNotBlank(cookieName)) {
            val cookie = request.getCookies().getFirst(cookieName);
            if (cookie != null) {
                accessToken = cookie.getName();
                if (log.isTraceEnabled()) {
                    log.trace("Try to get token from cookie({}): {}", cookieName, accessToken);
                }
            }
        }

        return accessToken;
    }

}

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

package com.apzda.kalami.security.authentication.handler;

import com.apzda.kalami.data.Response;
import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.event.LoginSuccessEvent;
import com.apzda.kalami.security.token.TokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import java.io.IOException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@RefreshScope
public class DefaultAuthenticationHandler implements AuthenticationHandler {

    private final SecurityConfigProperties properties;

    private final TokenManager tokenManager;

    @Getter
    private final ObjectMapper objectMapper;

    private final ApplicationEventPublisher eventPublisher;

    @Getter
    @Value("${kalami.security.login-url:}")
    private String loginUrl;

    @Getter
    @Value("${kalami.security.realm-name:}")
    private String realmName;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        if (log.isTraceEnabled()) {
            log.trace("Authentication Success: {}", authentication);
        }

        if (authentication instanceof JwtTokenAuthentication authenticationToken) {
            try {
                var jwtToken = authenticationToken.getJwtToken();
                if (jwtToken == null) {
                    jwtToken = tokenManager.create(authentication);
                    authenticationToken.setJwtToken(jwtToken);
                }

                val cookieCfg = properties.getCookie();
                val cookieName = cookieCfg.getCookieName();

                if (StringUtils.isNotBlank(cookieName)) {
                    response.addCookie(cookieCfg.createCookie(jwtToken));
                }

                eventPublisher.publishEvent(new LoginSuccessEvent(authentication));
                respond(request, response, Response.success(jwtToken));
            }
            catch (Exception e) {
                log.error("Create token failed: {}", e.getMessage(), e);

                respond(request, response, Response.error(ServiceError.SERVICE_UNAVAILABLE.code, e.getMessage()));
            }
        }
        else {
            log.error("Authentication is not a JwtAuthenticationToken instance!");
            respond(request, response, Response.error(ServiceError.INVALID_PRINCIPAL_TYPE));
        }
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        if (log.isTraceEnabled()) {
            log.trace("Authentication Failure: {}", exception.getMessage());
        }
        handleAuthenticationException(request, response, exception);
    }

    @Override
    public void onAccessDenied(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (log.isTraceEnabled()) {
            log.trace("Access Denied: {}", accessDeniedException.getMessage());
        }
        if (!response.isCommitted()) {
            respond(request, response, Response.error(ServiceError.FORBIDDEN));
        }
        else {
            throw accessDeniedException;
        }
    }

    @Override
    public void onUnauthorized(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        if (log.isTraceEnabled()) {
            log.trace("Unauthorized: {}", exception.getMessage());
        }
        handleAuthenticationException(request, response, exception);
    }

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request,
            HttpServletResponse response) throws SessionAuthenticationException {
        log.trace("Session is valid: {}", authentication);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            if (log.isTraceEnabled()) {
                log.trace("Logout: {}", authentication);
            }
            tokenManager.remove(authentication);
            this.onLogoutSuccess(request, response, authentication);
        }
        catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.trace("Token Manager cannot remove authentication data: {}", authentication, e);
            }
        }
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        respond(request, response, Response.error(ServiceError.UNAUTHORIZED));
    }

}

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
package com.apzda.kalami.security.authentication;

import com.apzda.kalami.security.authentication.handler.AuthenticationHandler;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.util.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public abstract class AuthenticationUtil {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationUtil.class);

    private static final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy();

    private static Lazy<AuthenticationManager> loader;

    private static SecurityContextRepository securityContextRepository;

    private static ApplicationEventPublisher eventPublisher;

    private static AuthenticationHandler handler;

    public AuthenticationUtil(@Nonnull AuthenticationManagerBuilder builder,
            SecurityContextRepository securityContextRepository, ApplicationEventPublisher eventPublisher,
            AuthenticationHandler handler) {
        AuthenticationUtil.loader = Lazy.of(builder::getOrBuild);
        AuthenticationUtil.handler = handler;
        AuthenticationUtil.eventPublisher = eventPublisher;
        AuthenticationUtil.securityContextRepository = securityContextRepository;
    }

    public static void authenticate(@Nonnull Authentication authentication, HttpServletRequest request,
            HttpServletResponse response) {
        try {
            val authenticationManager = loader.get();
            val authenticationResult = authenticationManager.authenticate(authentication);

            if (authenticationResult == null) {
                throw new InternalAuthenticationServiceException("Authentication failed");
            }

            handler.onAuthentication(authenticationResult, request, response);

            successfulAuthentication(request, response, authenticationResult);
        }
        catch (InternalAuthenticationServiceException failed) {
            logger.error("An internal error occurred while trying to authenticate the user.", failed);
            unsuccessfulAuthentication(request, response, failed);
        }
        catch (AuthenticationException ex) {
            unsuccessfulAuthentication(request, response, ex);
        }
    }

    public static void logout(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) {
        Authentication authentication = null;
        try {
            val context = SecurityContextHolder.getContext();
            if (context != null) {
                authentication = context.getAuthentication();
                if (authentication != null) {
                    handler.logout(request, response, authentication);
                }
            }
        }
        finally {
            if (authentication != null && eventPublisher != null) {
                eventPublisher.publishEvent(new LogoutSuccessEvent(authentication));
            }
        }
    }

    static void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            Authentication authResult) {
        try {
            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authResult);
            securityContextHolderStrategy.setContext(context);

            // 登录成功，保存上下文
            securityContextRepository.saveContext(context, request, response);

            if (logger.isDebugEnabled()) {
                logger.debug("Set SecurityContextHolder to {}", authResult);
            }

            handler.onAuthenticationSuccess(request, response, authResult);
        }
        catch (Exception ex) {
            logger.error("An internal error occurred while trying to authenticate the user.", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    static void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) {
        securityContextHolderStrategy.clearContext();
        logger.trace("Failed to process authentication request", failed);
        logger.trace("Cleared SecurityContextHolder");
        logger.trace("Handling authentication failure");
        try {
            handler.onAuthenticationFailure(request, response, failed);
        }
        catch (Exception ex) {
            logger.warn("Failed to handle authentication failure", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

}

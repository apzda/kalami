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
import com.apzda.kalami.data.error.IError;
import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.exception.BizException;
import com.apzda.kalami.security.error.AuthenticationError;
import com.apzda.kalami.security.error.InvalidTokenError;
import com.apzda.kalami.security.exception.InvalidSessionException;
import com.apzda.kalami.security.exception.UnRealAuthenticatedException;
import com.apzda.kalami.utils.MediaTypeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import java.io.IOException;

import static com.apzda.kalami.security.utils.SecurityUtils.CONTEXT_ATTR_EXCEPTION;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
public interface AuthenticationHandler
        extends AuthenticationFailureHandler, AuthenticationSuccessHandler, AccessDeniedHandler, LogoutHandler,
        SessionAuthenticationStrategy, AuthenticationEntryPoint, LogoutSuccessHandler {

    Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class);

    void onAccessDenied(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException;

    void onUnauthorized(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException;

    String getLoginUrl();

    String getRealmName();

    ObjectMapper getObjectMapper();

    @Override
    default void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        onAccessDenied(request, response, accessDeniedException);
    }

    @Override
    default void commence(@Nonnull HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        val exception = request.getAttribute(CONTEXT_ATTR_EXCEPTION);
        if (exception != null) {
            onUnauthorized(request, response, (AuthenticationException) exception);
        }
        else {
            onUnauthorized(request, response, authException);
        }
    }

    default void handleAuthenticationException(@Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response, @Nonnull Exception exception) throws IOException {
        if (!response.isCommitted()) {
            logger.trace("Authentication Exception caught and handled: {}", exception.getMessage());
            val error = getAuthenticationError(exception);
            respond(request, response, error);
        }
        else {
            logger.error("Authentication Exception cannot be handled for response which was commited", exception);
        }
    }

    default void respond(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
            @Nonnull Response<?> data) throws IOException {
        val errCode = data.getErrCode();
        val serverHttpRequest = new ServletServerHttpRequest(request);
        val mediaTypes = serverHttpRequest.getHeaders().getAccept();

        if (MediaTypeUtil.isText(mediaTypes) || MediaTypeUtil.isImage(mediaTypes)) {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE + ";charset=utf-8");
            if (errCode == 401) {
                val loginUrl = getLoginUrl();
                if (StringUtils.isNotBlank(loginUrl)) {
                    response.sendRedirect(loginUrl); // status is 302
                }
                else {
                    val realm = StringUtils.defaultIfBlank(getRealmName(), "Realm");
                    response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
                    response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
                }
                return;
            }
            else if (errCode == 200) {
                val loginUrl = getLoginUrl();
                if (StringUtils.isNotBlank(loginUrl)) {
                    response.sendRedirect(loginUrl); // status is 302
                    return;
                }
            }
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8");
        response.setStatus(data.getHttpCode());
        val jsonStr = getObjectMapper().writeValueAsString(data);

        try (val writer = response.getWriter()) {
            writer.write(jsonStr);
        }
        finally {
            response.flushBuffer();
        }
    }

    @Nonnull
    static Response<?> getAuthenticationError(@Nonnull Exception exception) {
        if (exception instanceof UsernameNotFoundException) {
            return Response.error(ServiceError.USER_PWD_INCORRECT).withErrMsg(exception.getMessage());
        }
        else if (exception instanceof AuthenticationError authenticationError) {
            return Response.error(authenticationError.getError());
        }
        else if (exception instanceof AccountStatusException statusException) {
            return handleAccountStatusException(statusException).withErrMsg(statusException.getMessage());
        }
        else if (exception instanceof BizException gsvcException) {
            return Response.error(gsvcException.getError());
        }
        else {
            return Response.error(ServiceError.UNAUTHORIZED).withErrMsg(exception.getMessage());
        }
    }

    @Nonnull
    static Response<?> handleAccountStatusException(@Nonnull AccountStatusException exception) {
        IError error;
        if (exception instanceof CredentialsExpiredException) {
            error = ServiceError.CREDENTIALS_EXPIRED;
        }
        else if (exception instanceof AccountExpiredException) {
            error = ServiceError.ACCOUNT_EXPIRED;
        }
        else if (exception instanceof LockedException) {
            error = ServiceError.ACCOUNT_LOCKED;
        }
        else if (exception instanceof UnRealAuthenticatedException) {
            error = ServiceError.ACCOUNT_UN_AUTHENTICATED;
        }
        else if (exception instanceof InvalidSessionException) {
            val message = exception.getMessage();
            if (StringUtils.isNotBlank(message)) {
                error = new InvalidTokenError(message);
            }
            else {
                error = ServiceError.TOKEN_INVALID;
            }
        }
        else {
            error = ServiceError.ACCOUNT_DISABLED;
        }

        return Response.error(error);
    }

}

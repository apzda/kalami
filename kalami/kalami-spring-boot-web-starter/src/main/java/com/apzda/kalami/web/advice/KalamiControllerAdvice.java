/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.web.advice;

import com.apzda.kalami.data.Response;
import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.exception.BizException;
import com.apzda.kalami.exception.DegradedException;
import com.apzda.kalami.exception.INoStackLog;
import com.apzda.kalami.http.ExceptionTransformer;
import com.apzda.kalami.i18n.I18n;
import com.apzda.kalami.utils.MediaTypeUtil;
import com.apzda.kalami.web.error.KalamiErrorController;
import jakarta.annotation.Nonnull;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.SocketException;
import java.net.URI;
import java.rmi.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
@Order
public class KalamiControllerAdvice {

    private final static ThreadLocal<Boolean> handledBox = new ThreadLocal<>();

    private final KalamiErrorController errorController;

    private final ObjectProvider<List<HttpMessageConverter<?>>> httpMessageConverters;

    private final ObjectProvider<List<ExceptionTransformer>> transformers;

    @Getter
    @Value("${kalami.security.login-url:}")
    private String loginUrl;

    @Getter
    @Value("${kalami.security.realm-name:}")
    private String realmName;

    @ExceptionHandler(Exception.class)
    public Object handleException(Exception error, HttpServletRequest request, HttpServletResponse response) {
        try {
            val serverRequest = ServerRequest.create(request,
                    httpMessageConverters.getIfAvailable(Collections::emptyList));
            val resp = handle(error, serverRequest, ResponseEntity.class);
            if (Boolean.TRUE.equals(handledBox.get())) {
                return resp;
            }

            val mediaTypes = serverRequest.headers().accept();

            if (MediaTypeUtil.isText(mediaTypes) || MediaTypeUtil.isImage(mediaTypes)) {
                if (request.getAttribute("jakarta.servlet.error.exception") == null) {
                    request.setAttribute("jakarta.servlet.error.exception", error);
                }
                request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, resp.getStatusCode().value());
                return errorController.errorHtml(request, response);
            }

            return resp;
        }
        finally {
            handledBox.remove();
        }
    }

    public Response<?> handle(Throwable e) {
        return handle(e, false);
    }

    public Response<?> handle(Throwable throwable, boolean transform) {
        val e = transform ? transform(throwable) : throwable;
        if (e instanceof BizException gsvcException) {
            val error = gsvcException.getError();
            return Response.error(error);
        }
        else if (e instanceof BindException bindException) {
            val violations = new HashMap<String, String>();
            for (FieldError error : bindException.getFieldErrors()) {
                violations.put(error.getField(), I18n.t(error));
            }
            return Response.error(ServiceError.BIND_ERROR, violations);
        }
        else if (e instanceof HttpMessageConversionException readableException) {
            return Response.error(ServiceError.INVALID_FORMAT, readableException.getMessage());
        }
        else if (e instanceof MethodArgumentTypeMismatchException typeMismatchException) {
            val violations = new HashMap<String, String>();
            violations.put(typeMismatchException.getName(), e.getMessage());
            return Response.error(ServiceError.BIND_ERROR, violations);
        }
        else if (e instanceof TimeoutException || e instanceof AsyncRequestTimeoutException) {
            return Response.error(ServiceError.SERVICE_TIMEOUT);
        }
        else if (e instanceof UnknownHostException || e instanceof SocketException) {
            return Response.error(ServiceError.REMOTE_SERVICE_NO_INSTANCE);
        }
        else if (e instanceof HttpStatusCodeException codeException) {
            return handleHttpStatusError(codeException.getStatusCode(), codeException.getMessage());
        }
        else if (e instanceof DegradedException) {
            return Response.error(ServiceError.DEGRADE);
        }
        else if (e instanceof ErrorResponse errorResponse) {
            val body = errorResponse.getBody();
            return handleHttpStatusError(errorResponse.getStatusCode(), body.getDetail());
        }
        else if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            return Response.error(ServiceError.SERVICE_ERROR).withErrMsg(e.getMessage());
        }

        return Response.error(ServiceError.SERVICE_ERROR);
    }

    public <R> R handle(Throwable throwable, ServerRequest request, Class<R> rClazz) {
        val error = transform(throwable);
        if (error instanceof HttpStatusCodeException || error instanceof ErrorResponse
                || error instanceof BizException) {
            val handled = checkLoginRedirect(request, error, rClazz);
            if (handled != null) {
                handledBox.set(Boolean.TRUE);
                return handled;
            }
        }
        ResponseWrapper responseWrapper;
        if (error instanceof BizException bizException) {
            val err = bizException.getError();
            if (err != null) {
                responseWrapper = ResponseWrapper.status(err.httpCode()).body(handle(error));
            }
            else {
                responseWrapper = ResponseWrapper.status(HttpStatus.SERVICE_UNAVAILABLE).body(handle(error));
            }
            responseWrapper.headers(bizException.getHeaders());
        }
        else if (error instanceof TimeoutException || error instanceof AsyncRequestTimeoutException) {
            responseWrapper = ResponseWrapper.status(HttpStatus.GATEWAY_TIMEOUT).body(handle(error));

            return responseWrapper.unwrap(rClazz, error);
        }
        else if (error instanceof UnknownHostException || error instanceof SocketException) {
            // rpc exception
            responseWrapper = ResponseWrapper.status(HttpStatus.BAD_GATEWAY).body(handle(error));

            return responseWrapper.unwrap(rClazz, error);
        }
        else if (error instanceof ErrorResponse responseException) {
            responseWrapper = ResponseWrapper.status(responseException.getStatusCode()).body(handle(error));
            responseWrapper.headers(responseException.getHeaders());

            return responseWrapper.unwrap(rClazz, error);
        }
        else if (error instanceof HttpStatusCodeException httpStatusCodeException) {
            responseWrapper = ResponseWrapper.status(httpStatusCodeException.getStatusCode()).body(handle(error));
            responseWrapper.headers(httpStatusCodeException.getResponseHeaders());

            return responseWrapper.unwrap(rClazz, error);
        }
        else if (error instanceof BindException || error instanceof HttpMessageConversionException
                || error instanceof MethodArgumentTypeMismatchException) {
            responseWrapper = ResponseWrapper.status(HttpStatus.BAD_REQUEST).body(handle(error));

            return responseWrapper.unwrap(rClazz, error);
        }
        else {
            responseWrapper = ResponseWrapper.status(HttpStatus.INTERNAL_SERVER_ERROR).body(handle(error));
        }
        if (!(error instanceof INoStackLog)) {
            log.error("Exception Resolved:", error);
        }

        return responseWrapper.unwrap(rClazz, null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <R> R checkLoginRedirect(@Nonnull ServerRequest request, Throwable e, Class<R> rClass) {
        HttpStatusCode status = HttpStatusCode.valueOf(500);
        HttpHeaders headers = new HttpHeaders();
        if (e instanceof ErrorResponse statusException) {
            status = statusException.getStatusCode();
            headers = new HttpHeaders(statusException.getHeaders());
        }
        else if (e instanceof HttpStatusCodeException statusCodeException) {
            status = statusCodeException.getStatusCode();
            if (statusCodeException.getResponseHeaders() != null) {
                headers = new HttpHeaders(statusCodeException.getResponseHeaders());
            }
        }
        else if (e instanceof BizException bizException) {
            val error = bizException.getError();
            if (error != null) {
                if (error.headers() != null) {
                    headers = error.headers();
                }
            }
        }

        val mediaTypes = request.headers().accept();
        var mediaType = mediaTypes.isEmpty() ? MediaType.APPLICATION_JSON : mediaTypes.get(0).removeQualityValue();
        if (mediaType.isWildcardType()) {
            mediaType = MediaType.APPLICATION_JSON;
        }
        else if (mediaType.isWildcardSubtype()) {
            mediaType = switch (mediaType.getType()) {
                case "*", "text" -> MediaType.TEXT_PLAIN;
                case "application" -> MediaType.APPLICATION_JSON;

                default -> throw new IllegalStateException("Unexpected value: " + mediaType.getType());
            };
        }

        if (status == HttpStatus.UNAUTHORIZED) {
            val url = MediaTypeUtil.getUrl(loginUrl, mediaTypes);

            if (StringUtils.isNotBlank(url)) {
                if (rClass.isAssignableFrom(ServerResponse.class)) {
                    return (R) ServerResponse.status(HttpStatus.TEMPORARY_REDIRECT)
                        .contentType(mediaType)
                        .location(URI.create(url))
                        .build();
                }
                else {
                    return (R) ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                        .location(URI.create(url))
                        .contentType(mediaType)
                        .build();
                }
            }
            else {
                val realm = MediaTypeUtil.getUrl(StringUtils.defaultIfBlank(realmName, "Realm"), mediaTypes);
                if (StringUtils.isBlank(realm)) {
                    return null;
                }

                if (rClass.isAssignableFrom(ServerResponse.class)) {
                    return (R) ServerResponse.status(HttpStatus.UNAUTHORIZED)
                        .contentType(mediaType)
                        .headers(headers1 -> {
                            headers1.add("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
                        })
                        .build();
                }
                else {
                    return (R) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"" + realm + "\"")
                        .contentType(mediaType)
                        .build();
                }
            }
        }

        return null;
    }

    private Throwable transform(@Nonnull Throwable throwable) {
        while (throwable.getClass().equals(RuntimeException.class) && throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        if (throwable instanceof IllegalArgumentException || throwable instanceof IllegalStateException
                || throwable instanceof BizException) {
            return throwable;
        }

        val ts = transformers.getIfAvailable();
        if (ts == null) {
            return throwable;
        }
        val aClass = throwable.getClass();

        for (ExceptionTransformer t : ts) {
            if (t.supports(aClass)) {
                val te = t.transform(throwable);
                if (te != null) {
                    if (te.getCause() instanceof BizException) {
                        return te.getCause();
                    }
                    return te;
                }
            }
        }

        return throwable;
    }

    @Nonnull
    private Response<?> handleHttpStatusError(@Nonnull HttpStatusCode statusCode, String message) {
        return Response.error(statusCode.value(), StringUtils.defaultIfBlank(message, statusCode.toString()));
    }

    static class ResponseWrapper {

        private HttpStatusCode status;

        private Response<?> body;

        private HttpHeaders headers = new HttpHeaders();

        @Nonnull
        static ResponseWrapper status(HttpStatusCode status) {
            val wrapper = new ResponseWrapper();
            wrapper.status = status;
            return wrapper;
        }

        @Nonnull
        static ResponseWrapper status(int status) {
            val wrapper = new ResponseWrapper();
            try {
                wrapper.status = HttpStatusCode.valueOf(status);
            }
            catch (Exception e) {
                wrapper.status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            return wrapper;
        }

        ResponseWrapper body(Response<?> body) {
            this.body = body;
            return this;
        }

        void headers(HttpHeaders headers) {
            if (headers != null) {
                this.headers = headers;
            }
        }

        @SuppressWarnings("unchecked")
        public <R> R unwrap(Class<R> rClazz, @Nullable Throwable error) {
            if (error != null) {
                log.debug("Exception Resolved[{}]: {}", error.getClass().getName(), error.getMessage());
            }

            if (rClazz.isAssignableFrom(ServerResponse.class)) {
                return (R) ServerResponse.status(status).headers(httpHeaders -> {
                    httpHeaders.putAll(this.headers);
                }).body(body);
            }
            else {
                return (R) ResponseEntity.status(status).headers(headers).body(body);
            }
        }

    }

}

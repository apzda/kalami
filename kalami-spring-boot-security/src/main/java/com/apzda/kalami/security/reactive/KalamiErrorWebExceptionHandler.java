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

package com.apzda.kalami.security.reactive;

import com.apzda.kalami.data.Response;
import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.exception.BizException;
import com.apzda.kalami.utils.MediaTypeUtil;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
@Slf4j
public class KalamiErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    @Getter
    @Value("${kalami.security.login-url:}")
    private String loginUrl;

    @Getter
    @Value("${kalami.security.realm-name:}")
    private String realmName;

    public KalamiErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources,
            ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, resources, applicationContext);
        setMessageReaders(serverCodecConfigurer.getReaders());
        setMessageWriters(serverCodecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(request -> true, this::handleError);
    }

    @Nonnull
    public Mono<ServerResponse> handleError(@Nonnull ServerRequest request) {
        val error = this.getError(request);
        if (error != null) {
            log.trace("抛异常啦: {}", error.getClass().getName());
            val login = checkLoginRedirect(request, error);
            if (login != null) {
                return login;
            }


        }

        ServerResponse.BodyBuilder builder = ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE);

        return builder.bodyValue(Response.error(ServiceError.SERVICE_UNAVAILABLE));
    }

    protected void logError(ServerRequest request, ServerResponse response, Throwable throwable) {
        if (log.isDebugEnabled()) {
            log.debug("{}", request.exchange().getLogPrefix() + formatError(throwable, request));
        }
        if (HttpStatus.resolve(response.statusCode().value()) != null
                && response.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
            log.error("{}", LogMessage.of(() -> String.format("%s 500 Server Error for %s",
                    request.exchange().getLogPrefix(), formatRequest(request))));
        }
    }

    private String formatError(Throwable ex, ServerRequest request) {
        String reason = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        return "Resolved [" + reason + "] for HTTP " + request.method() + " " + request.path();
    }

    private String formatRequest(ServerRequest request) {
        String rawQuery = request.uri().getRawQuery();
        String query = StringUtils.hasText(rawQuery) ? "?" + rawQuery : "";
        return "HTTP " + request.method() + " \"" + request.path() + query + "\"";
    }

    @Nullable
    private Mono<ServerResponse> checkLoginRedirect(@Nonnull ServerRequest request, Throwable e) {
        HttpStatusCode status = HttpStatusCode.valueOf(503);
        if (e instanceof ErrorResponse statusException) {
            status = statusException.getStatusCode();
        }
        else if (e instanceof HttpStatusCodeException statusCodeException) {
            status = statusCodeException.getStatusCode();

        }
        else if (e instanceof BizException bizException) {
            val error = bizException.getError();
            if (error != null) {
                status = HttpStatusCode.valueOf(error.httpCode());
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

            if (org.apache.commons.lang3.StringUtils.isNotBlank(url)) {
                ServerResponse.BodyBuilder builder = ServerResponse.status(HttpStatus.TEMPORARY_REDIRECT)
                    .contentType(mediaType)
                    .location(URI.create(url));
                return builder.build();
            }
            else {
                val realm = MediaTypeUtil
                    .getUrl(org.apache.commons.lang3.StringUtils.defaultIfBlank(realmName, "Realm"), mediaTypes);
                if (org.apache.commons.lang3.StringUtils.isBlank(realm)) {
                    return null;
                }

                ServerResponse.BodyBuilder builder = ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .contentType(mediaType)
                    .headers(headers1 -> {
                        headers1.add("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
                    });
                return builder.build();
            }
        }

        return null;
    }

}

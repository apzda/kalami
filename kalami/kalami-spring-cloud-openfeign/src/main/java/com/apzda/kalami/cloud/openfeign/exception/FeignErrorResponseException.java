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
package com.apzda.kalami.cloud.openfeign.exception;

import cn.hutool.core.util.URLUtil;
import com.apzda.kalami.exception.INoStackLog;
import com.apzda.kalami.i18n.I18n;
import com.apzda.kalami.utils.StringUtil;
import feign.FeignException;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@Slf4j
public class FeignErrorResponseException extends ErrorResponseException implements INoStackLog {

    public FeignErrorResponseException(HttpStatusCode status, Throwable cause) {
        super(getRealHttpStatus(status, cause), from(status, cause), cause);
    }

    @Nonnull
    static ProblemDetail from(HttpStatusCode status, @Nonnull Throwable exception) {
        val problemDetail = ProblemDetail.forStatusAndDetail(getRealHttpStatus(status, exception),
                exception.getMessage());
        val builder = new StringBuilder(exception.getMessage());
        builder.append("\n");

        val httpCode = problemDetail.getStatus();
        if (exception instanceof FeignException feignException) {
            val request = feignException.request();

            if (request != null) {
                val url = request.url();
                val host = URLUtil.url(url);
                if (httpCode == 401 || httpCode == 403) {
                    problemDetail.setDetail(null);
                }
                else if (host != null) {
                    val svcName = host.getHost();
                    if (svcName != null) {
                        problemDetail.setDetail(I18n.t("feign.service.503", new Object[] { svcName }));
                    }
                }
                if (httpCode != 401 && httpCode != 403) {
                    val charset = Optional.ofNullable(request.charset()).orElse(StandardCharsets.UTF_8);
                    val body = StringUtil.convert(request.body(), charset, StandardCharsets.UTF_8);
                    val headers = request.headers();
                    val filtered = new HttpHeaders();
                    headers.forEach((key, value) -> {
                        filtered.addAll(key, value.stream().toList());
                    });
                    filtered.remove("Authorization");

                    builder.append(String.format("""
                            【接  口】%s %s
                            【请求头】%s
                            【请求体】%s
                            """, request.httpMethod().name(), url, filtered, body));
                }
            }
            if (httpCode != 401 && httpCode != 403) {
                builder.append(String.format("""
                        【响应码】%d
                        【响应头】%s
                        【响应体】%s
                        """, feignException.status(), feignException.responseHeaders(), feignException.contentUTF8()));
            }
        }

        log.error("Feign Call Error: {}", builder.toString().trim());

        return problemDetail;
    }

    static HttpStatusCode getRealHttpStatus(HttpStatusCode status, Throwable cause) {
        if (cause instanceof FeignException feignException) {
            val code = feignException.status();
            if (code >= 100 && code <= 999) {
                return HttpStatusCode.valueOf(code);
            }
        }
        return status;
    }

}

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
package com.apzda.kalami.cloud.openfeign.exception;

import cn.hutool.core.util.URLUtil;
import com.apzda.kalami.data.error.IError;
import com.apzda.kalami.exception.NoStackBizException;
import com.apzda.kalami.utils.StringUtil;
import feign.FeignException;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
class RemoteServiceException extends NoStackBizException {

    public RemoteServiceException(@Nonnull IError error, FeignException feignException) {
        super(error, feignException);
        logError(feignException);
    }

    void logError(@Nonnull FeignException exception) {
        val builder = new StringBuilder(exception.getMessage()).append("\n");
        val httpCode = exception.status();
        val request = exception.request();
        String host = "";

        if (request != null) {
            val url = request.url();
            host = URLUtil.url(url).getHost();
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
                        【请求头】%s
                        【请求体】%s
                        """, filtered, body));
            }
        }

        if (httpCode != 401 && httpCode != 403) {
            builder.append(String.format("""
                    【响应码】%d
                    【响应头】%s
                    【响应体】%s
                    """, exception.status(), exception.responseHeaders(), exception.contentUTF8()));
        }

        log.error("Remote Service('{}') Error: {}", host, builder.toString().trim());
    }

}

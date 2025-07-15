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
package com.apzda.kalami.web.interceptor;

import com.apzda.kalami.http.Encrypted;
import com.apzda.kalami.web.config.KalamiServerProperties;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class EncryptedHandlerInterceptor implements HandlerInterceptor {

    private final static MediaType encryptedMediaType = new MediaType("application", "encrypted+json");

    private final KalamiServerProperties properties;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
            @Nonnull Object handler) throws Exception {
        if (!properties.isEncryptedEnabled()) {
            return true;
        }

        if (handler instanceof HandlerMethod hm) {
            val method = hm.getMethod();
            if (method.isAnnotationPresent(Encrypted.class) || hm.getBeanType().isAnnotationPresent(Encrypted.class)) {
                try {
                    val mediaType = MediaType.valueOf(request.getContentType());
                    val accept = MediaType.valueOf(request.getHeader("Accept"));
                    if (mediaType.isCompatibleWith(encryptedMediaType) && accept.isCompatibleWith(encryptedMediaType)) {
                        return true;
                    }
                    throw new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                }
                catch (Exception ex) {
                    throw new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                }
            }
            return true;
        }

        return true;
    }

}

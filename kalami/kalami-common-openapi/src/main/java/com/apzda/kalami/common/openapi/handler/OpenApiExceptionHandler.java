/*
 * Copyright 2026 the original author or authors.
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
package com.apzda.kalami.common.openapi.handler;

import com.apzda.kalami.common.openapi.RequestHolder;
import com.apzda.kalami.common.openapi.Response;
import com.apzda.kalami.common.openapi.exception.OpenApiException;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 开放接口异常处理器
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RestControllerAdvice
@Order
@Slf4j
public class OpenApiExceptionHandler {
    @ExceptionHandler(OpenApiException.class)
    public Response<?> handleOpenApiException(@Nonnull OpenApiException e) {
        val head = new Response.Head(RequestHolder.getHead());
        log.debug("OPENAPI: 异常[{}]已处理: {}", head.getClientId(), e.getClass());
        head.setAlgorithm("PLAIN");

        return Response.error(head, e.getRespCode(), e.getMessage());
    }
}

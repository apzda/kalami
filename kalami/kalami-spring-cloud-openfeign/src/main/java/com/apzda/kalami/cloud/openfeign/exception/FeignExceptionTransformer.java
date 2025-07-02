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

import com.apzda.kalami.data.MessageType;
import com.apzda.kalami.data.error.AbstractBaseError;
import com.apzda.kalami.http.ExceptionTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.util.Collections;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@RequiredArgsConstructor
public class FeignExceptionTransformer implements ExceptionTransformer {

    private final ObjectMapper objectMapper;

    @Override
    public Exception transform(Throwable exception) {
        if (exception instanceof FeignException feignException) {
            val body = feignException.contentUTF8();
            if (StringUtils.isNotBlank(body)) {
                val contentType = feignException.responseHeaders()
                    .getOrDefault("content-type", Collections.emptyList());
                if (contentType.contains("application/json")) {
                    try {
                        val error = objectMapper.readValue(body, FeignError.class);
                        if (error.getErrCode() != null) {
                            error.setHttCode(feignException.status());
                            return new RemoteServiceException(error, feignException);
                        }
                    }
                    catch (JsonProcessingException ignored) {
                    }
                }
            }
        }

        return new FeignErrorResponseException(HttpStatus.SERVICE_UNAVAILABLE, exception);
    }

    @Override
    public boolean supports(Class<? extends Throwable> eClass) {
        return FeignException.class.isAssignableFrom(eClass);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class FeignError extends AbstractBaseError {

        private Integer errCode;

        private int httCode;

        private String errMsg;

        private MessageType type;

        @Override
        public int code() {
            return errCode;
        }

        @Override
        public int httpCode() {
            return httCode;
        }

        @Override
        public String localMessage() {
            return errMsg;
        }

    }

}

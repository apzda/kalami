/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.cloud.openfeign.transfomer;

import com.apzda.kalami.cloud.openfeign.exception.FeignErrorResponseException;
import com.apzda.kalami.http.ExceptionTransformer;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
public class FeignExceptionTransformer implements ExceptionTransformer {

    @Override
    public ErrorResponseException transform(Throwable exception) {
        return new FeignErrorResponseException(HttpStatus.SERVICE_UNAVAILABLE, exception);
    }

    @Override
    public boolean supports(Class<? extends Throwable> eClass) {
        return FeignException.class.isAssignableFrom(eClass);
    }

}

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
package com.apzda.kalami.exception;

import com.apzda.kalami.data.error.IError;
import com.apzda.kalami.data.error.ServiceNotAvailableError;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpHeaders;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class CommonBizException extends BizException implements INoStackLog {

    public CommonBizException(String message) {
        super(message);
    }

    public CommonBizException(int code, String message) {
        super(new IError() {
            @Override
            public int code() {
                return code;
            }

            @Override
            public String message() {
                return message;
            }
        });
    }

    public CommonBizException(String message, Throwable cause) {
        super(new ServiceNotAvailableError(message), cause);
    }

    public CommonBizException(@Nonnull IError error, HttpHeaders headers, Throwable e) {
        super(error, headers, e);
    }

    public CommonBizException(@Nonnull IError error, HttpHeaders headers) {
        super(error, headers);
    }

    public CommonBizException(@Nonnull IError error, Throwable e) {
        super(error, e);
    }

    public CommonBizException(@Nonnull IError error) {
        super(error);
    }

}

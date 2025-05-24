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
package com.apzda.kalami.exception;

import com.apzda.kalami.data.error.IError;
import com.apzda.kalami.data.error.ServiceNotAvailableError;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.HttpHeaders;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Getter
public class BizException extends RuntimeException {

    protected final IError error;

    private final HttpHeaders headers;

    public BizException(String message) {
        this(new ServiceNotAvailableError(message), null, null);
    }

    public BizException(@Nonnull IError error, HttpHeaders headers, Throwable e) {
        super(error.localMessage(), e);
        this.error = error;
        this.headers = headers == null ? HttpHeaders.EMPTY : headers;
    }

    public BizException(@Nonnull IError error, HttpHeaders headers) {
        this(error, headers, null);
    }

    public BizException(@Nonnull IError error, Throwable e) {
        this(error, null, e);
    }

    public BizException(@Nonnull IError error) {
        this(error, null, null);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("errCode", error.code())
            .append("errMsg", error.localMessage())
            .toString();
    }

}

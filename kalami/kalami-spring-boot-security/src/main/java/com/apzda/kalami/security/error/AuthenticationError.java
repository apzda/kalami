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

package com.apzda.kalami.security.error;

import com.apzda.kalami.data.error.IError;
import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.exception.INoStackLog;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Getter
public class AuthenticationError extends AuthenticationException implements INoStackLog {

    protected final IError error;

    public AuthenticationError(@Nonnull IError error, Throwable cause) {
        super(error.message(), cause);
        this.error = error;
    }

    public AuthenticationError(@Nonnull IError error) {
        super(error.message());
        this.error = error;
    }

    public AuthenticationError() {
        super(ServiceError.UNAUTHORIZED.message());
        this.error = ServiceError.UNAUTHORIZED;
    }

    @Override
    public String toString() {
        return "AuthenticationError{" + "error=" + error + '}';
    }

}

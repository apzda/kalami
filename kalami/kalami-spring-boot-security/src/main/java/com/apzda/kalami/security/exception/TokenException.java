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

package com.apzda.kalami.security.exception;

import com.apzda.kalami.data.error.IError;
import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.security.error.AuthenticationError;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
public class TokenException extends AuthenticationError {

    public final static AuthenticationError EXPIRED = new TokenException(ServiceError.TOKEN_EXPIRED);

    public static final AuthenticationError INVALID_TOKEN = new TokenException(ServiceError.TOKEN_INVALID);

    public static final AuthenticationError DEVICE_NOT_ALLOWED = new TokenException(ServiceError.DEVICE_NOT_ALLOWED);

    TokenException(IError error) {
        super(error);
    }

}

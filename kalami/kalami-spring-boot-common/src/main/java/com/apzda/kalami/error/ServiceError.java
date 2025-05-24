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
package com.apzda.kalami.error;

import com.apzda.kalami.data.error.IError;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
public enum ServiceError implements IError {

    //@formatter:off
    BAD_REQUEST(-400, HttpStatus.BAD_REQUEST.getReasonPhrase(),400),
    UNAUTHORIZED(-401, HttpStatus.UNAUTHORIZED.getReasonPhrase(),401),
    FORBIDDEN(-403, HttpStatus.FORBIDDEN.getReasonPhrase(),403),
    NOT_FOUND(-404, HttpStatus.NOT_FOUND.getReasonPhrase(),404),
    METHOD_NOT_ALLOWED(-405, HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),405),
    TOO_MANY_REQUESTS(-429, HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),429),
    SERVICE_ERROR(-500, "Service Internal Error",500),
    REMOTE_SERVICE_ERROR(-501, "Service RPC Error",503),
    REMOTE_SERVICE_NO_INSTANCE(-502, "No Service instance found",503),
    SERVICE_UNAVAILABLE(-503, HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),503),
    SERVICE_TIMEOUT(-504, "Service RPC Timeout",504),
    INVALID_PRINCIPAL_TYPE(-800, "Unknown Principal type",403),
    MFA_NOT_SETUP(-801,"Mfa not setup",403),
    MFA_NOT_VERIFIED(-802,"Mfa not verified",403),
    TOKEN_EXPIRED(-810,"Access Token expired",401),
    TOKEN_INVALID(-811,"Token is invalid",401),
    USER_PWD_INCORRECT(-812,"Username or Password is incorrect",403),
    CREDENTIALS_EXPIRED(-813,"Credentials is expired",403),
    ACCOUNT_EXPIRED(-814,"Account is expired",403),
    ACCOUNT_LOCKED(-815,"Account is locked",403),
    ACCOUNT_UN_AUTHENTICATED(-816,"Account is unAuthenticated",403),
    ACCOUNT_DISABLED(-817,"Account is disabled",403),
    DEVICE_NOT_ALLOWED(-818,"Device is not allowed",403),
    DATABASE_ERROR(-995, "Database Error",500),
    INVALID_FORMAT(-996, "Invalid Format",400),
    BIND_ERROR(-997, "Data Is Invalid",400),
    DEGRADE(-998, "Service has been Degraded",503),
    JACKSON_ERROR(-999, "Invalid JSON data",400);
    //@formatter:on
    @JsonValue
    public final int code;

    public final String message;

    public final String fallbackString;

    private final int httpCode;

    ServiceError(int code, String message, int httpCode) {
        this.code = code;
        this.message = message;
        this.fallbackString = """
                {"errCode":%d,"errMsg":"%s"}
                """.formatted(code, message());
        this.httpCode = httpCode;
    }

    @Override
    public int code() {
        return code;
    }

    public int httpCode() {
        return this.httpCode;
    }

    @Override
    @Nonnull
    public String toString() {
        return String.format("[%d]%s - %s", code(), message(), Arrays.toString(args()));
    }

}

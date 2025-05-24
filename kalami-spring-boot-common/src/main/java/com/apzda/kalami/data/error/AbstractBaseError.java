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
package com.apzda.kalami.data.error;

import com.apzda.kalami.data.MessageType;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public abstract class AbstractBaseError implements IError {

    protected Object[] args;

    private String message;

    private MessageType type;

    private HttpHeaders headers;

    public AbstractBaseError(Object... args) {
        this.args = args;
    }

    public AbstractBaseError withMessage(String message) {
        this.message = message;
        return this;
    }

    public AbstractBaseError withType(MessageType type) {
        this.type = type;
        return this;
    }

    public AbstractBaseError withHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return this.headers;
    }

    @Override
    public String message() {
        return this.message;
    }

    @Override
    public MessageType type() {
        return this.type;
    }

    @Override
    public Object[] args() {
        return this.args;
    }

    @Override
    @Nonnull
    public String toString() {
        return String.format("[%d]%s - %s", code(), message(), Arrays.toString(args()));
    }

}

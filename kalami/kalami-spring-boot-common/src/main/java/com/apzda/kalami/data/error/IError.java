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
import com.apzda.kalami.exception.BizException;
import com.apzda.kalami.i18n.I18n;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public interface IError {

    Object[] emptyArgs = new Object[] {};

    int code();

    default int httpCode() {
        return 500;
    }

    default HttpHeaders headers() {
        return null;
    }

    default String message() {
        return "";
    }

    default String localMessage() {
        val message = message();
        val code = code();
        if (code != 0 && StringUtils.isBlank(message)) {
            return I18n.t("error." + Math.abs(code), args());
        }
        else if (StringUtils.startsWith(message, "{") && StringUtils.endsWith(message, "}")) {
            val msg = message.substring(1, message.length() - 1);
            return I18n.t(msg, args());
        }

        return message;
    }

    default Object[] args() {
        return emptyArgs;
    }

    default MessageType type() {
        return null;
    }

    default void emit() {
        throw new BizException(this);
    }

    default void emit(Throwable e) {
        throw new BizException(this, null, e);
    }

    default void emit(HttpHeaders headers, Throwable e) {
        throw new BizException(this, headers, e);
    }

}

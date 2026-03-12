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
package com.apzda.kalami.common.openapi;

import com.apzda.kalami.i18n.I18n;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public abstract class Request<T extends Serializable> implements Serializable {

    @Serial
    private static final long serialVersionUID = -4307150905555953986L;

    /**
     * 请求头
     */
    private Head head = new Head();

    public Request() {

    }

    public Request(Head head) {
        this(head, null);
    }

    public Request(Head head, T data) {
        this.head = head;
        this.setData(data);
    }

    public void setHead(Head head) {
        this.head = head;
        RequestHolder.setHead(head);
    }

    public abstract void setData(T data);

    public abstract T getData();

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Head implements Serializable {

        @Serial
        private static final long serialVersionUID = 9133046594010236023L;

        /**
         * 版本号
         */
        private String version = "1.0.0";

        /**
         * 语言
         */
        private String language;

        /**
         * 客户ID
         */
        private String clientId = "";

        /**
         * 请求时间戳
         */
        private Long timestamp = 0L;

        /**
         * 加密算法
         */
        private String algorithm = "AES";

        public void setLocale(Locale locale) {
            if (locale == null) {
                return;
            }

            I18n.setLocale(locale);
            I18n.setDefaultLocale(locale);
            LocaleContextHolder.setLocale(locale);
            this.language = locale.toLanguageTag();
        }

    }

}

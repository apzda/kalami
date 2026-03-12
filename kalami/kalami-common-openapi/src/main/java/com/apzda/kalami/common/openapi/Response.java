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

import cn.hutool.core.date.DateUtil;
import com.apzda.kalami.i18n.I18n;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.val;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1074718718581462323L;

    @JsonIgnore
    private transient Head head;

    /**
     * 响应码
     */
    private String code;

    /**
     * 响应说明
     */
    private String msg;

    /**
     * 响应时间
     */
    private Long timestamp;

    /**
     * 语言
     */
    @JsonIgnore
    private String language;

    /**
     * 版本
     */
    @JsonIgnore
    private String version;

    /**
     * 加密算法
     */
    @JsonIgnore
    private String algorithm;

    /**
     * 响应数据
     */
    private T data;

    Response() {
    }

    Response(Request.Head head, T body) {
        this.head = new Head(head);
        this.data = body;
    }

    Response(Head head, T body) {
        this.head = head;
        this.data = body;
    }

    public String getCode() {
        return Optional.ofNullable(code).orElseGet(() -> this.head.getRespCode());
    }

    public String getMsg() {
        return Optional.ofNullable(msg).orElseGet(() -> this.head.getRespDesc());
    }

    public Long getTimestamp() {
        return Optional.ofNullable(timestamp).orElseGet(() -> this.head.getTimestamp());
    }

    public String getLanguage() {
        return Optional.ofNullable(language).orElseGet(() -> this.head.getLanguage());
    }

    public String getAlgorithm() {
        return Optional.ofNullable(algorithm).orElseGet(() -> this.head.getAlgorithm());
    }

    public String getVersion() {
        return Optional.ofNullable(version).orElseGet(() -> this.head.getVersion());
    }

    @Nonnull
    public static <T> Response<T> success(@Nonnull Head head, T body) {
        return new Response<>(head, body);
    }

    @Nonnull
    public static <T> Response<T> success(@Nonnull Request.Head head, T body) {
        return new Response<>(head, body);
    }

    @Nonnull
    public static <T> Response<T> error(@Nonnull Request.Head head, String code, String msg) {
        val resp = new Response<T>(head, null);
        resp.getHead().setRespCode(code);
        resp.getHead().setRespDesc(msg);
        return resp;
    }

    @Nonnull
    public static <T> Response<T> error(@Nonnull Response.Head head, String code, String msg) {
        val resp = new Response<T>(head, null);
        resp.getHead().setRespCode(code);
        resp.getHead().setRespDesc(msg);
        return resp;
    }

    @Nonnull
    public static <T> Response<T> error(@Nonnull Request.Head head, String code) {
        val resp = new Response<T>(head, null);
        resp.getHead().setRespCode(code);
        resp.getHead().setRespDesc(I18n.t(String.format("api.error.%s", code)));

        return resp;
    }

    @Nonnull
    public static <T> Response<T> error(@Nonnull Response.Head head, String code) {
        val resp = new Response<T>(head, null);
        resp.getHead().setRespCode(code);
        resp.getHead().setRespDesc(I18n.t(String.format("api.error.%s", code)));

        return resp;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Head implements Serializable {

        @Serial
        private static final long serialVersionUID = 7404070330462151566L;

        /**
         * 响应时间戳（秒)
         */
        private Long timestamp;

        /**
         * 响应码
         */
        private String respCode;

        /**
         * 响应说明
         */
        private String respDesc;

        /**
         * 版本号
         */
        private String version = "1.0.0";

        /**
         * 语言
         */
        private String language;

        private String clientId = "";

        /**
         * 加密算法
         */
        private String algorithm;

        public Head() {
            this.timestamp = DateUtil.currentSeconds();
            this.respCode = "200";
            this.language = "en-US";
            this.clientId = "";
        }

        public Head(@Nonnull Request.Head head) {
            this.timestamp = DateUtil.currentSeconds();
            this.version = head.getVersion();
            this.language = head.getLanguage();
            this.clientId = head.getClientId();
            this.algorithm = head.getAlgorithm();
            this.respCode = "200";
        }

    }

}

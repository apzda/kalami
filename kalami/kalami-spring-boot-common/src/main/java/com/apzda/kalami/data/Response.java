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

package com.apzda.kalami.data;

import cn.hutool.core.util.ObjectUtil;
import com.apzda.kalami.data.error.IError;
import com.apzda.kalami.i18n.I18n;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应
 *
 * @author fengz (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2865776613817344888L;

    /**
     * 错误码(Error Code): 0 means success, others value mean error.
     */
    private int errCode;

    /**
     * 错误提示(Error Message)
     */
    private String errMsg;

    /**
     * 错误提示类型(Type)
     */
    private MessageType type;

    @JsonIgnore
    private int httpCode = 200;

    /**
     * 响应数据
     */
    private T data;

    void setMessage(String message) {
        if (Strings.CS.startsWith(message, "{") && Strings.CS.endsWith(message, "}")) {
            val text = message.substring(1, message.length() - 1);
            val msg = I18n.t(text);
            if (StringUtils.isNotBlank(msg)) {
                this.errMsg = msg;
            }
            else {
                this.errMsg = message;
            }
        }
        else {
            this.errMsg = message;
        }
    }

    @Nonnull
    public Response<T> type(MessageType type) {
        this.type = type;
        return this;
    }

    @Nonnull
    public Response<T> alert(String message) {
        this.type = MessageType.ALERT;
        setMessage(message);
        return this;
    }

    @Nonnull
    public Response<T> notify(String message) {
        this.type = MessageType.NOTIFY;
        setMessage(message);
        return this;
    }

    @Nonnull
    public Response<T> toast(String message) {
        this.type = MessageType.TOAST;
        setMessage(message);
        return this;
    }

    @Nonnull
    public Response<T> none(String message) {
        this.type = MessageType.NONE;
        setMessage(message);
        return this;
    }

    public void setErrType(String errType) {
        this.type = MessageType.fromString(errType);
    }

    public void setErrType(MessageType errType) {
        this.type = errType;
    }

    public Response<T> withHttpCode(int httpCode) {
        this.httpCode = httpCode;
        return this;
    }

    public Response<T> withErrMsg(String message) {
        if (StringUtils.isNotBlank(message)) {
            setMessage(message);
        }
        return this;
    }

    public Response<T> withType(MessageType errType) {
        this.type = errType;
        return this;
    }

    @JsonIgnore
    public boolean isSuccess() {
        if (data instanceof Boolean rst) {
            return rst && errCode == 0;
        }

        return errCode == 0;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return ObjectUtil.isEmpty(this.data);
    }

    @JsonIgnore
    public boolean isNotEmpty() {
        return ObjectUtil.isNotEmpty(this.data);
    }

    @JsonIgnore
    public boolean isNull() {
        return ObjectUtil.isNull(this.data);
    }

    @JsonIgnore
    public boolean isNotNull() {
        return ObjectUtil.isNotNull(this.data);
    }

    @Nonnull
    public T orElse(@Nonnull T defaultValue) {
        return data == null ? defaultValue : data;
    }

    @Nullable
    public T get() {
        return data;
    }

    @Nonnull
    public static <T> Response<T> wrap(@Nonnull T data) {
        Assert.notNull(data, "data is null!");
        val resp = new Response<T>();
        BeanUtils.copyProperties(data, resp, "data", "type", "message");
        resp.setData(data);
        return resp;
    }

    @Nonnull
    public static <T> Response<T> wrap(@Nonnull Response<?> response) {
        Assert.notNull(response, "response is null!");
        val resp = new Response<T>();
        BeanUtils.copyProperties(response, resp);
        return resp;
    }

    @Nonnull
    public static <T> Response<T> error(@Nonnull IError error) {
        Response<T> resp = error(error.code(), error.localMessage());
        resp.type = error.type();
        resp.httpCode = error.httpCode();
        return resp;
    }

    @Nonnull
    public static <T> Response<T> error(@Nonnull IError error, T data) {
        Response<T> resp = error(error.code(), error.localMessage());
        resp.type = error.type();
        resp.data = data;
        resp.httpCode = error.httpCode();
        return resp;
    }

    @Nonnull
    public static <T> Response<T> error(String message) {
        return error(700, message);
    }

    @Nonnull
    public static <T> Response<T> error(int code, String errMsg) {
        val resp = new Response<T>();
        resp.errCode = code;
        resp.errMsg = errMsg;
        if (Strings.CS.startsWith(errMsg, "{") && Strings.CS.endsWith(errMsg, "}")) {
            resp.setMessage(errMsg);
        }
        else if (StringUtils.isBlank(errMsg) && errMsg == null) {
            val msg = I18n.t("error." + Math.abs(code));
            if (StringUtils.isNotBlank(msg)) {
                resp.errMsg = msg;
            }
        }

        return resp;
    }

    @Nonnull
    public static <T> Response<T> error(int code) {
        return error(code, null);
    }

    @Nonnull
    public static <T> Response<T> success(T data, String message) {
        val resp = new Response<T>();
        resp.setData(data);
        resp.setMessage(message);
        return resp;
    }

    @Nonnull
    public static <T> Response<T> success(T data) {
        return success(data, null);
    }

    @Nonnull
    public static <T> Response<T> ok(String message) {
        return success(null, message);
    }

    @Nonnull
    public static <T> Response<T> ok() {
        return success(null);
    }

    @Nonnull
    public static <T> Response<T> empty() {
        return success(null);
    }

    @Nonnull
    public static <T> Response<T> of(T data) {
        return success(data);
    }

    @Nonnull
    public static <T> T orElse(Response<T> response, @Nonnull T defaultValue) {
        if (response == null) {
            return defaultValue;
        }
        if (response.data == null) {
            return defaultValue;
        }
        return response.data;
    }

    @Nullable
    public static <T> T get(Response<T> response) {
        if (response == null) {
            return null;
        }
        return response.data;
    }

    public static boolean isSuccess(Response<?> response) {
        if (response == null) {
            return false;
        }
        return response.isSuccess();
    }

}

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
package com.apzda.kalami.common.openapi.sign;

import com.apzda.kalami.common.openapi.Request;
import com.apzda.kalami.common.openapi.Response;
import com.apzda.kalami.common.openapi.exception.OpenApiException;

/**
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface OpenApiSigner {
    /**
     * 验证签名
     *
     * @param head 请求头
     * @param data 正文
     */
    void verify(Request.Head head, byte[] data, String signature) throws OpenApiException;

    /**
     * 签名
     *
     * @param head 响应头
     * @param data 正文
     * @return 签名
     */
    String sign(Response.Head head, byte[] data) throws OpenApiException;
}

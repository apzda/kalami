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
package com.apzda.kalami.common.openapi.modem;

import com.apzda.kalami.common.openapi.Request;
import com.apzda.kalami.common.openapi.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;

/**
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class PlainModem implements Modem {
    @Override
    public boolean supports(String algorithm) {
        return StringUtils.isBlank(algorithm) || "PLAIN".equalsIgnoreCase(algorithm);
    }

    @Override
    public byte[] decode(Request.Head head, byte[] encodedText) throws IOException, HttpMessageNotReadableException {
        return encodedText;
    }

    @Override
    public byte[] encode(Response.Head head, byte[] plainText) throws IOException, HttpMessageNotWritableException {
        return plainText;
    }
}

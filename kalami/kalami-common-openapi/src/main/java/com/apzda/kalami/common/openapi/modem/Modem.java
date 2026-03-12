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

import jakarta.annotation.Nonnull;
import com.apzda.kalami.common.openapi.Request;
import com.apzda.kalami.common.openapi.Response;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface Modem {
    boolean supports(String algorithm);

    default byte[] decode(Request.Head head, @Nonnull InputStream stream) throws
                                                                          IOException,
                                                                          HttpMessageNotReadableException {
        return decode(head, stream.readAllBytes());
    }

    byte[] decode(Request.Head head, byte[] encodedText) throws IOException, HttpMessageNotReadableException;

    default byte[] encode(Response.Head head, @Nonnull String plainText) throws
                                                                         IOException,
                                                                         HttpMessageNotWritableException {
        return encode(head, plainText.getBytes(StandardCharsets.UTF_8));
    }

    default byte[] encode(Response.Head head, @Nonnull String plainText, @Nonnull Charset charset) throws IOException {
        return encode(head, plainText.getBytes(charset));
    }

    byte[] encode(Response.Head head, byte[] plainText) throws IOException, HttpMessageNotWritableException;

}

/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.http.modem;

import jakarta.annotation.Nonnull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
public interface Modem {

    default byte[] decode(HttpHeaders headers, @Nonnull InputStream stream)
            throws IOException, HttpMessageNotReadableException {
        return decode(headers, stream.readAllBytes());
    }

    byte[] decode(HttpHeaders headers, byte[] encodedText) throws IOException, HttpMessageNotReadableException;

    default byte[] encode(HttpHeaders headers, @Nonnull String plainText)
            throws IOException, HttpMessageNotWritableException {
        return encode(headers, plainText.getBytes(StandardCharsets.UTF_8));
    }

    default byte[] encode(HttpHeaders headers, @Nonnull String plainText, @Nonnull Charset charset) throws IOException {
        return encode(headers, plainText.getBytes(charset));
    }

    byte[] encode(HttpHeaders headers, byte[] plainText) throws IOException, HttpMessageNotWritableException;

}

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
package com.apzda.kalami.web.converter;

import com.apzda.kalami.http.modem.Modem;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EncryptedMessageConverter extends AbstractHttpMessageConverter<Object> {

    private static final String subType = "encrypted+json";

    private final ObjectMapper objectMapper;

    private final Modem modem;

    public EncryptedMessageConverter(ObjectMapper objectMapper, Modem modem) {
        super(StandardCharsets.UTF_8, new MediaType("application", subType));
        this.objectMapper = objectMapper;
        this.modem = modem;
    }

    @Override
    protected boolean supports(@Nonnull Class<?> clazz) {
        return !BeanUtils.isSimpleProperty(clazz);
    }

    @Override
    @Nonnull
    protected Object readInternal(@Nonnull Class<?> clazz, @Nonnull HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        val headers = inputMessage.getHeaders();
        return objectMapper.readValue(modem.decode(headers, inputMessage.getBody()), clazz);
    }

    @Override
    protected void writeInternal(@Nonnull Object o, @Nonnull HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        val headers = outputMessage.getHeaders();
        outputMessage.getBody().write(modem.encode(headers, objectMapper.writeValueAsString(o)));
    }

    @Override
    protected boolean canRead(MediaType mediaType) {
        if (mediaType == null) {
            return false;
        }
        if (!subType.equals(mediaType.getSubtype())) {
            return false;
        }
        for (MediaType supportedMediaType : getSupportedMediaTypes()) {
            if (supportedMediaType.includes(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        if (mediaType == null || MediaType.ALL.equalsTypeAndSubtype(mediaType)) {
            return false;
        }
        if (!subType.equals(mediaType.getSubtype())) {
            return false;
        }
        for (MediaType supportedMediaType : getSupportedMediaTypes()) {
            if (supportedMediaType.isCompatibleWith(mediaType)) {
                return true;
            }
        }
        return false;
    }

}

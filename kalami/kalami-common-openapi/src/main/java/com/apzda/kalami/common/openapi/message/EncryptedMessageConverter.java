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
package com.apzda.kalami.common.openapi.message;

import com.apzda.kalami.common.openapi.Request;
import com.apzda.kalami.common.openapi.RequestHolder;
import com.apzda.kalami.common.openapi.Response;
import com.apzda.kalami.common.openapi.SimpleDataRequest;
import com.apzda.kalami.common.openapi.config.OpenApiProperties;
import com.apzda.kalami.common.openapi.exception.IllegalRequestBody;
import com.apzda.kalami.common.openapi.exception.NoSupportedAlgException;
import com.apzda.kalami.common.openapi.exception.OpenApiException;
import com.apzda.kalami.common.openapi.modem.Modem;
import com.apzda.kalami.common.openapi.sign.OpenApiSigner;
import com.apzda.kalami.i18n.I18n;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class EncryptedMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final ObjectMapper objectMapper;

    private final ObjectProvider<Modem> modems;

    private final OpenApiSigner signer;

    @Value("${kalami.openapi.simple-mode:false}")
    private boolean simpleMode;

    @Value("${kalami.openapi.encrypt-mode:BODY}")
    private OpenApiProperties.EncryptMode encryptMode;

    @Value("${kalami.openapi.algorithm:AES}")
    private String defaultAlg;

    @Override
    protected boolean supports(@Nonnull Class<?> clazz) {
        return Request.class.isAssignableFrom(clazz) || Response.class.equals(clazz);
    }

    @Override
    @Nonnull
    protected Object readInternal(@Nonnull Class<?> clazz, @Nonnull HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        val headers = inputMessage.getHeaders();
        Locale locale = LocaleUtils.toLocale("en-US");
        try {
            val acceptLanguageAsLocales = headers.getAcceptLanguageAsLocales();
            if (!CollectionUtils.isEmpty(acceptLanguageAsLocales)) {
                locale = acceptLanguageAsLocales.get(0);
            }
        }
        catch (Exception e) {
            log.warn("OPENAPI： 非法的Accept-Language: {}", headers.getFirst("Accept-Language"));
        }
        setLocale(locale);

        try {
            if (simpleMode) {
                // 简易模式下不验签，不解密
                val request = (Request<?>) objectMapper.readValue(inputMessage.getBody(), clazz);
                setLocale(request.getHead());
                return request;
            }

            val signature = headers.getFirst("X-SIGN");
            val body = StreamUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);
            val strReq = objectMapper.readValue(body, new TypeReference<SimpleDataRequest>() {
            });
            val head = strReq.getHead();
            setLocale(head);

            // 验证签名
            signer.verify(head, body.getBytes(StandardCharsets.UTF_8), signature);

            val modem = getSupportedModem(head.getAlgorithm());
            val data = strReq.getData();
            if (data != null) {
                val dataStr = new String(modem.decode(head, Base64.getDecoder().decode(data)), StandardCharsets.UTF_8);
                val content = body.replace(String.format("\"%s\"", data), dataStr);
                return objectMapper.readValue(content, clazz);
            }
            else {
                return objectMapper.readValue(inputMessage.getBody(), clazz);
            }
        }
        catch (JsonProcessingException e) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes();
            assert attributes != null;
            val request = attributes.getRequest();
            log.error("无法解析OPENAPI请求:\nURL: {}\nCLAZZ: {}\nERROR: {}", request.getRequestURI(), clazz, e.getMessage());
            throw new IllegalRequestBody(e.getMessage());
        }
    }

    @Override
    protected void writeInternal(@Nonnull Object o, @Nonnull HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        val headers = outputMessage.getHeaders();
        headers.set("Content-Type", "application/json");

        Response<?> resp = (Response<?>) o;
        val head = resp.getHead();
        byte[] content;

        val data = ((Response<?>) o).getData();
        if (data != null) {
            try {
                if (OpenApiProperties.EncryptMode.BODY.equals(encryptMode)) {
                    Modem modem = getSupportedModem(head.getAlgorithm());
                    val dataBytes = objectMapper.writeValueAsBytes(data);
                    val encrypted = Base64.getEncoder().encodeToString(modem.encode(head, dataBytes));
                    resp = Response.success(head, encrypted);
                    headers.set("X-ALGORITHM", head.getAlgorithm());
                    headers.set("Content-Type", "application/encrypted+json");
                }
            }
            catch (OpenApiException e) {
                resp = Response.error(head, e.getRespCode());
            }
            catch (Exception e) {
                resp = Response.error(head, "S00007");
            }
        }

        if (!"PLAIN".equalsIgnoreCase(head.getAlgorithm()) && OpenApiProperties.EncryptMode.ALL.equals(encryptMode)) {
            try {
                Modem modem = getSupportedModem(head.getAlgorithm());
                val dataBytes = objectMapper.writeValueAsBytes(resp);
                content = Base64.getEncoder().encode(modem.encode(head, dataBytes));
                headers.set("X-ALGORITHM", head.getAlgorithm());
                headers.set("Content-Type", "application/encrypted+json");
            }
            catch (OpenApiException e) {
                content = objectMapper.writeValueAsBytes(Response.error(head, e.getRespCode()));
            }
            catch (Exception e) {
                content = objectMapper.writeValueAsBytes(Response.error(head, "S00007"));
            }
        }
        else {
            content = objectMapper.writeValueAsBytes(resp);
        }

        if (!simpleMode) {
            val sign = signer.sign(head, content);
            headers.set("X-SIGN", sign);
        }

        outputMessage.getBody().write(content);
    }

    @Nonnull
    private Modem getSupportedModem(String algorithm) throws NoSupportedAlgException {
        final String alg;
        if (StringUtils.isBlank(algorithm)) {
            alg = defaultAlg;
        }
        else {
            alg = algorithm;
        }

        for (Modem modem : modems.orderedStream().toList()) {
            if (modem.supports(alg)) {
                return modem;
            }
        }

        throw new NoSupportedAlgException(algorithm);
    }

    @Override
    protected boolean canRead(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.isCompatibleWith(mediaType);
    }

    private static void setLocale(Request.Head head) {
        try {
            val language = head.getLanguage();
            if (StringUtils.isNotBlank(language)) {
                val locale = LocaleUtils.toLocale(language);
                setLocale(locale);
            }
            else {
                head.setLanguage(LocaleContextHolder.getLocale().toLanguageTag());
            }
        }
        catch (Exception e) {
            log.warn("OPENAPI: 无法设置语言 - {}", e.getMessage());
        }
    }

    private static void setLocale(Locale locale) {
        I18n.setLocale(locale);
        I18n.setDefaultLocale(locale);
        LocaleContextHolder.setLocale(locale);
        RequestHolder.getHead().setLanguage(locale.toLanguageTag());
    }

}

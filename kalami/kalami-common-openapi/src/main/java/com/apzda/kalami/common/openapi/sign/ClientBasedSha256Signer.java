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

import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.DigestUtil;
import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.apzda.kalami.common.openapi.ClientInfoProvider;
import com.apzda.kalami.common.openapi.Request;
import com.apzda.kalami.common.openapi.Response;
import com.apzda.kalami.common.openapi.exception.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 默认签名器。签名算法如下:  hex(sha256(content+pubkey+timestamp))
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Data
public class ClientBasedSha256Signer implements OpenApiSigner {
    private final ClientInfoProvider provider;

    @Value("${kalami.openapi.leeway:30}")
    private long leeway;

    @Override
    public void verify(Request.Head head, byte[] data, String signature) throws OpenApiException {
        val clientId = head.getClientId();
        if (!StringUtils.hasText(clientId)) {
            throw new MissingRequiredHeaderException("X-CLIENT-ID");
        }
        val timestamp = head.getTimestamp();
        if (timestamp == null) {
            throw new MissingRequiredHeaderException("TIMESTAMP");
        }

        if (leeway > 0) {
            val ctime = DateUtil.currentSeconds();
            if (Math.abs(timestamp - ctime) > leeway) {
                throw new IllegalTimestampException();
            }
        }

        if (!StringUtils.hasText(signature)) {
            throw new MissingRequiredHeaderException("X-SIGN");
        }
        val clientInfo = provider.getClientInfo(clientId);
        val clientPubKey = clientInfo.getClientPublicKey();
        if (!StringUtils.hasText(clientPubKey)) {
            throw new IllegalClientIdException();
        }

        if (!verify(signature, new String(data, StandardCharsets.UTF_8), clientPubKey, String.valueOf(timestamp))) {
            throw new IllegalSignException();
        }
    }

    @Override
    @Nonnull
    public String sign(Response.@NonNull Head head, byte[] content) throws OpenApiException {
        val resp = new String(content, StandardCharsets.UTF_8);
        val timestamp = head.getTimestamp();
        val clientId = head.getClientId();
        val clientInfo = provider.getClientInfo(clientId);
        val serverPublicKey = clientInfo.getServerPublicKey();

        return sign(resp, serverPublicKey, String.valueOf(timestamp));
    }

    public static boolean verify(String signature, String content, String key, String timestamp) throws
                                                                                                 OpenApiException {
        key = StringUtils.replace(key, "\n", "");
        val sign1 = DigestUtil.sha256Hex(String.format("%s%s%s", content, key, timestamp));

        return sign1.equals(signature);
    }

    public static String sign(String content, String key, String timestamp) throws OpenApiException {
        key = StringUtils.replace(key, "\n", "");
        return DigestUtil.sha256Hex(String.format("%s%s%s", content, key, timestamp).getBytes(StandardCharsets.UTF_8));
    }
}

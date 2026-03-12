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

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import com.apzda.kalami.common.openapi.ClientInfoProvider;
import com.apzda.kalami.common.openapi.Request;
import com.apzda.kalami.common.openapi.Response;
import com.apzda.kalami.common.openapi.exception.IllegalCertificateException;
import com.apzda.kalami.common.openapi.exception.IllegalClientIdException;
import com.apzda.kalami.common.openapi.exception.MissingRequiredHeaderException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ClientBasedRsaModem implements Modem {
    private final ClientInfoProvider provider;

    @Override
    public boolean supports(String algorithm) {
        return "RSA".equalsIgnoreCase(algorithm);
    }

    @Override
    public byte[] decode(@Nonnull Request.Head head, byte[] encodedText) throws
                                                                         IOException,
                                                                         HttpMessageNotReadableException {
        val clientId = head.getClientId();
        if (!StringUtils.hasText(clientId)) {
            throw new MissingRequiredHeaderException("clientId");
        }
        val clientInfo = provider.getClientInfo(clientId);
        val clientPubKey = clientInfo.getClientPublicKey();
        if (!StringUtils.hasText(clientPubKey)) {
            log.warn("OPENAPI: 客户证书公钥未配置[{}]。", clientId);
            throw new IllegalClientIdException();
        }
        val clientPrivateKey = clientInfo.getClientPrivateKey();
        if (!StringUtils.hasText(clientPubKey)) {
            log.warn("OPENAPI: 客户证书私钥未配置[{}]。", clientId);
            throw new IllegalClientIdException();
        }
        try {
            return SecureUtil.rsa(clientPrivateKey, clientPubKey).decrypt(encodedText, KeyType.PublicKey);
        } catch (Exception e) {
            log.error("OPENAPI： 解密请求出错[{}] - {}", clientId, e.getMessage());
            throw new IllegalCertificateException(e.getMessage());
        }
    }

    @Override
    public byte[] encode(Response.Head head, byte[] plainText) throws IOException, HttpMessageNotWritableException {
        val clientId = head.getClientId();
        val clientInfo = provider.getClientInfo(clientId);
        val serverPrivateKey = clientInfo.getServerPrivateKey();
        val serverPublicKey = clientInfo.getServerPublicKey();

        try {
            return SecureUtil.rsa(serverPrivateKey, serverPublicKey).encrypt(plainText, KeyType.PrivateKey);
        } catch (Exception e) {
            log.error("OPENAPI： 加密响应出错 - {}", e.getMessage());
            throw new IllegalCertificateException(e.getMessage());
        }
    }
}

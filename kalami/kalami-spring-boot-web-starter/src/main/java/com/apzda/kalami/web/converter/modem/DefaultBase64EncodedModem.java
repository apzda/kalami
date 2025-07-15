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
package com.apzda.kalami.web.converter.modem;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.DES;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import com.apzda.kalami.http.modem.Modem;
import com.apzda.kalami.infra.config.InfraConfigProperties;
import jakarta.annotation.Nonnull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
public class DefaultBase64EncodedModem implements Modem {

    private final SymmetricCrypto crypto;

    public DefaultBase64EncodedModem(@Nonnull InfraConfigProperties.ModemConfig modemConfig) {
        if (modemConfig.getAlgorithm() == InfraConfigProperties.Algorithm.AES) {
            if (modemConfig.getMode().equals("ECB")) {
                crypto = new AES(modemConfig.getMode(), modemConfig.getPadding(),
                        modemConfig.getKey().getBytes(StandardCharsets.UTF_8), null);
            }
            else {
                crypto = new AES(modemConfig.getMode(), modemConfig.getPadding(),
                        modemConfig.getKey().getBytes(StandardCharsets.UTF_8),
                        modemConfig.getIv().getBytes(StandardCharsets.UTF_8));
            }
        }
        else {
            if (modemConfig.getMode().equals("ECB")) {
                crypto = new DES(modemConfig.getMode(), modemConfig.getPadding(),
                        modemConfig.getKey().getBytes(StandardCharsets.UTF_8), null);
            }
            else {
                crypto = new DES(modemConfig.getMode(), modemConfig.getPadding(),
                        modemConfig.getKey().getBytes(StandardCharsets.UTF_8),
                        modemConfig.getIv().getBytes(StandardCharsets.UTF_8));
            }
        }

    }

    @Override
    public byte[] decode(HttpHeaders headers, byte[] encoded) throws IOException, HttpMessageNotReadableException {
        return crypto.decrypt(Base64.decode(encoded));
    }

    @Override
    public byte[] encode(HttpHeaders headers, byte[] plainText) throws IOException, HttpMessageNotWritableException {
        return Base64.encode(crypto.encrypt(plainText)).getBytes(StandardCharsets.UTF_8);
    }

}

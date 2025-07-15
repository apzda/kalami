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
package com.apzda.kalami.web.converter.modem;

import cn.hutool.crypto.Padding;
import com.apzda.kalami.http.modem.Modem;
import com.apzda.kalami.infra.config.InfraConfigProperties;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
class DefaultBase64EncodedModemTest {

    @Test
    void modem_aes_should_work() throws IOException {
        // given
        InfraConfigProperties.ModemConfig modemConfig = new InfraConfigProperties.ModemConfig();
        Modem modem = new DefaultBase64EncodedModem(modemConfig);
        String plainText = "Hello World!";
        String encrypted = "TihWFgE4RXmF2bNRUq95qg==";
        // when
        val encoded = modem.encode(null, plainText, StandardCharsets.UTF_8);
        val decoded = modem.decode(null, encoded);
        // then
        assertThat(new String(encoded)).isEqualTo(encrypted);
        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo(plainText);
    }

    @Test
    void modem_des_should_work() throws IOException {
        // given
        InfraConfigProperties.ModemConfig modemConfig = new InfraConfigProperties.ModemConfig();
        modemConfig.setAlgorithm(InfraConfigProperties.Algorithm.DES);
        modemConfig.setIv("10000001");
        Modem modem = new DefaultBase64EncodedModem(modemConfig);
        String plainText = "Hello World!";
        String encrypted = "5PcR8X7G8yT5oKVh1sU57A==";
        // when
        val encoded = modem.encode(null, plainText, StandardCharsets.UTF_8);
        val decoded = modem.decode(null, encoded);
        // then
        assertThat(new String(encoded)).isEqualTo(encrypted);
        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo(plainText);
    }

    @Test
    void modem_des_ecb_should_work() throws IOException {
        // given
        InfraConfigProperties.ModemConfig modemConfig = new InfraConfigProperties.ModemConfig();
        modemConfig.setAlgorithm(InfraConfigProperties.Algorithm.AES);
        modemConfig.setMode("ECB");
        modemConfig.setIv("0807060504030201");
        modemConfig.setKey("0CoJUm6Qyw8W8jud0CoJUm6Qyw8W8jud");
        modemConfig.setPadding(Padding.PKCS5Padding.name());
        Modem modem = new DefaultBase64EncodedModem(modemConfig);
        String encrypted = "uivcLF8SKjmShmUPIQ//jw==";
        // when
        val decoded = modem.decode(null, encrypted.getBytes(StandardCharsets.UTF_8));
        // then
        assertThat(new String(decoded, StandardCharsets.UTF_8)).isEqualTo("abcde");
    }

}

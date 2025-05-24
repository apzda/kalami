package com.apzda.kalami.web.converter.modem;

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

}

package com.apzda.kalami.web.converter;

import com.apzda.kalami.http.modem.Modem;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@JsonTest
@ContextConfiguration(classes = EncryptedMessageConverterTest.class)
class EncryptedMessageConverterTest {

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    Modem modem;

    @Test
    void converter_should_work() {
        // given
        MediaType mediaType = MediaType.APPLICATION_JSON;
        MediaType subType = MediaType.valueOf("application/encrypted+json");
        val encryptedMessageConverter = new EncryptedMessageConverter(objectMapper, modem);

        // then
        assertThat(encryptedMessageConverter.canRead(EncryptedMessageConverterTest.class, mediaType)).isFalse();
        assertThat(encryptedMessageConverter.canRead(EncryptedMessageConverterTest.class, mediaType)).isFalse();

        assertThat(encryptedMessageConverter.canWrite(EncryptedMessageConverterTest.class, subType)).isTrue();
        assertThat(encryptedMessageConverter.canWrite(EncryptedMessageConverterTest.class, subType)).isTrue();
    }

}

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
package com.apzda.hajimi.captcha.controller;

import com.apzda.kalami.data.Response;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "logging.level.com.apzda.hajimi.captcha=trace")
@AutoConfigureWebClient
class ValidateControllerTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void validate() throws Exception {
        // when
        val body = webClient.get()
            .uri("/captcha/biz")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-CAPTCHA-UUID", "1234")
            .header("X-CAPTCHA-ID", "1")
            .exchange()
            .expectBody(new ParameterizedTypeReference<Response<String>>() {
            })
            .returnResult()
            .getResponseBody();
        // then
        System.out.println("body = " + body);
        assertThat(body).isNotNull();
        assertThat(body.getErrCode()).isEqualTo(1);
    }

    @Test
    void validate1() throws Exception {
        // given
        val testRequest = new ValidateController.TestRequest();
        testRequest.setCaptchaId("1");
        testRequest.setCaptchaUuid("1234");
        // when
        val body = webClient.post()
            .uri("/captcha/bizx")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(testRequest)
            .exchange()
            .expectBody(new ParameterizedTypeReference<Response<String>>() {
            })
            .returnResult()
            .getResponseBody();
        // then
        assertThat(body).isNotNull();
        assertThat(body.getErrCode()).isEqualTo(1);

    }

}

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

package com.apzda.kalami.demo.server;

import com.apzda.kalami.data.Response;
import com.apzda.kalami.security.token.DefaultToken;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ApiGatewayItTest {

    private static DefaultToken token;

    @BeforeAll
    static void login(@Autowired @Nonnull WebTestClient webTestClient) {
        val responseBody = webTestClient.post()
            .uri("/login")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectBody(new ParameterizedTypeReference<Response<DefaultToken>>() {
            })
            .returnResult()
            .getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getErrCode()).isEqualTo(0);
        token = responseBody.getData();
    }

    @Test
    void test(@Autowired @Nonnull WebTestClient webTestClient) {
        webTestClient.post()
            .uri("/userSvc/user/heiehi")
            .accept(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new ParameterizedTypeReference<Response<String>>() {
            })
            .consumeWith(response -> {
                val body = response.getResponseBody();

                assertThat(body).isNotNull();
                val data = body.getData();
                assertThat(body.getErrCode()).isEqualTo(0);

                assertThat(data).isEqualTo("Heihei");
            });
    }

}

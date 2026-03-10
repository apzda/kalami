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
package com.apzda.hajimi.dy.openapi;

import com.apzda.hajimi.dy.openapi.credential.Credential;
import com.apzda.hajimi.dy.openapi.request.OauthClientTokenRequest;
import com.apzda.hajimi.dy.openapi.request.OpenGetTicketRequest;
import com.apzda.hajimi.dy.openapi.request.ShareIdRequest;
import com.apzda.hajimi.dy.openapi.response.OauthClientTokenResponse;
import com.apzda.hajimi.dy.openapi.response.OpenApiResponse;
import com.apzda.hajimi.dy.openapi.response.OpenGetTicketResponse;
import com.apzda.hajimi.dy.openapi.response.ShareIdResponse;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Retryable(retryFor = { HttpClientErrorException.Gone.class, HttpClientErrorException.TooManyRequests.class,
        HttpServerErrorException.BadGateway.class, HttpServerErrorException.GatewayTimeout.class,
        TimeoutException.class }, maxAttempts = 2, backoff = @Backoff(delay = 1500))
public class Client {

    private final Credential credential;

    private final OpenApi openApi;

    public Client(Config config) {
        this.credential = new Credential(config);
        int readTimeout = 30000;
        int connectTimeout = 1000;
        boolean ignoreSSL = true;

        val settings = ClientHttpRequestFactorySettings.defaults()
            .withConnectTimeout(Duration.ofSeconds(connectTimeout))
            .withReadTimeout(Duration.ofSeconds(readTimeout));

        val restClient = RestClient.builder()
            .baseUrl("https://open.douyin.com/")
            .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings))
            .build();

        val adapter = RestClientAdapter.create(restClient);
        val factory = HttpServiceProxyFactory.builderFor(adapter).build();

        this.openApi = factory.createClient(OpenApi.class);
    }

    public OpenApiResponse<OauthClientTokenResponse> oauthClientToken(
            @Validated @Nonnull OauthClientTokenRequest request) {
        return this.openApi.oauthClientToken(request);
    }

    public OpenApiResponse<OpenGetTicketResponse> openGetTicket(@Validated @Nonnull OpenGetTicketRequest request) {
        return this.openApi.openGetTicket(request.getAccessToken());
    }

    public OpenApiResponse<ShareIdResponse> shareId(@Validated @Nonnull ShareIdRequest request) {
        return this.openApi.shareId(request.getAccessToken(), request.isNeedCallback());
    }

}

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

import com.apzda.hajimi.dy.openapi.request.OauthClientTokenRequest;
import com.apzda.hajimi.dy.openapi.response.OauthClientTokenResponse;
import com.apzda.hajimi.dy.openapi.response.OpenApiResponse;
import com.apzda.hajimi.dy.openapi.response.OpenGetTicketResponse;
import com.apzda.hajimi.dy.openapi.response.ShareIdResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
interface OpenApi {

    @PostExchange("/oauth/client_token/")
    OpenApiResponse<OauthClientTokenResponse> oauthClientToken(@RequestBody OauthClientTokenRequest request);

    @HttpExchange(value = "/open/getticket/", method = "GET", contentType = MediaType.APPLICATION_JSON_VALUE,
            accept = MediaType.APPLICATION_JSON_VALUE)
    OpenApiResponse<OpenGetTicketResponse> openGetTicket(@RequestHeader("access-token") String clientToken);

    @HttpExchange(value = "/share-id/", method = "GET", contentType = MediaType.APPLICATION_JSON_VALUE,
            accept = MediaType.APPLICATION_JSON_VALUE)
    OpenApiResponse<ShareIdResponse> shareId(@RequestHeader("access-token") String clientToken,
            @RequestParam("need_callback") boolean needCallback);

}

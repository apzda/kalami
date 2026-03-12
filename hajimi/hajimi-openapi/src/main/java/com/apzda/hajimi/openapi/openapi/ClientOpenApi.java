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
package com.apzda.hajimi.openapi.openapi;

import com.apzda.hajimi.openapi.openapi.request.LoginRequest;
import com.apzda.kalami.common.openapi.ClientInfoProvider;
import com.apzda.kalami.common.openapi.RequestHolder;
import com.apzda.kalami.common.openapi.Response;
import com.apzda.kalami.common.openapi.annotation.OpenApi;
import com.apzda.kalami.common.openapi.exception.IllegalClientIdException;
import com.apzda.kalami.common.openapi.exception.OpenApiException;
import com.apzda.kalami.common.openapi.token.TokenManager;
import com.apzda.kalami.common.openapi.utils.IpUtil;
import com.apzda.kalami.i18n.I18n;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Token管理
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/open/client")
public class ClientOpenApi {

    private final ClientInfoProvider provider;

    private final TokenManager tokenManager;

    /**
     * 获取Token
     */
    @OpenApi(false)
    @PostMapping("/login")
    public Response<String> login(@RequestBody LoginRequest request) throws OpenApiException {
        try {
            val clientInfo = provider.getClientInfo(request.getClientId());
            val head = RequestHolder.getHead();
            head.setClientId(clientInfo.getClientId());

            if (clientInfo.isDisabled()) {
                throw new OpenApiException("401", I18n.t("api.error.clientIsDisabled"));
            }

            val ipBlacklist = clientInfo.getIpBlacklist();
            if (StringUtils.isNotBlank(ipBlacklist) && IpUtil.match(ipBlacklist, true)) {
                throw new OpenApiException("403", I18n.t("api.error.ipBlacklistMatch"));
            }

            val ipWhitelist = clientInfo.getIpWhitelist();
            if (StringUtils.isNotBlank(ipWhitelist) && !IpUtil.match(ipWhitelist, true)) {
                throw new OpenApiException("403", I18n.t("api.error.ipWhitelistMatch"));
            }

            val clientSecret = clientInfo.getClientSecret();
            if (!request.getClientSecret().equals(clientSecret)) {
                throw new OpenApiException("401", I18n.t("api.error.secretIsIncorrect"));
            }

            return Response.success(head, tokenManager.createToken(clientInfo.getClientId()));
        }
        catch (IllegalClientIdException e) {
            throw new OpenApiException("401", I18n.t("api.error.clientIdNotExist"));
        }
    }

}

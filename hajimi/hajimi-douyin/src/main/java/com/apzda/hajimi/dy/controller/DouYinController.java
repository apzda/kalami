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
package com.apzda.hajimi.dy.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import com.apzda.hajimi.dy.autoconfig.DouYinConfigProperties;
import com.apzda.hajimi.dy.dto.CommonCallbackDto;
import com.apzda.hajimi.dy.dto.ShareDto;
import com.apzda.hajimi.dy.event.CallbackEvent;
import com.apzda.hajimi.dy.event.ShareEvent;
import com.apzda.hajimi.dy.openapi.Client;
import com.apzda.hajimi.dy.openapi.request.ShareIdRequest;
import com.apzda.hajimi.dy.service.DouYinService;
import com.apzda.hajimi.dy.vo.ShareVo;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("${hajimi.dy.prefix:/open/dy}")
@RequiredArgsConstructor
public class DouYinController {

    private final DouYinService douYinService;

    private final Client client;

    private final DouYinConfigProperties properties;

    private final ApplicationEventPublisher publisher;

    /**
     * 回调
     */
    @PostMapping("/callback")
    public Object callback(@RequestBody CommonCallbackDto dto) throws IOException {
        log.debug("收到抖音回调: {}", dto);
        if ("verify_webhook".equals(dto.event())) {
            val clientKey = dto.clientKey();
            if (!Objects.equals(clientKey, properties.getClientKey())) {
                return "";
            }
            val content = dto.content(Map.class);
            if (content.isPresent()) {
                return String.format("{\"challenge\": %s}", content.get().get("challenge"));
            }
        }

        publisher.publishEvent(new CallbackEvent(dto));
        return "";
    }

    /**
     * 分享视频
     */
    @PostMapping("/share")
    public Response<ShareVo> share(@RequestBody @Validated ShareDto share,
            @CurrentUser.Required(value = false) CurrentUser user) throws Exception {
        String nonceStr = UUID.randomUUID().toString();
        Long timestamp = DateUtil.currentSeconds();
        String ticket = douYinService.getOpenTicket();
        String sigStr = String.format("nonce_str=%s&ticket=%s&timestamp=%s", nonceStr, ticket, timestamp);
        String signature = DigestUtils.md5Hex(sigStr);

        val request = new ShareIdRequest();
        request.setAccessToken(douYinService.getClientToken());
        // request.setDefaultHashtag("");
        request.setNeedCallback(true);
        val response = client.shareId(request);
        val shareId = response.getData().getShareId();

        val shareVo = BeanUtil.copyProperties(share, ShareVo.class);
        shareVo.setClientKey(properties.getClientKey());
        shareVo.setNonceStr(nonceStr);
        shareVo.setTimestamp(String.valueOf(timestamp));
        shareVo.setSignature(signature);
        shareVo.setShareId(shareId);
        shareVo.setAppId(properties.getAppId());
        shareVo.setAppUrl(properties.getAppUrl());
        shareVo.setAppTitle(properties.getAppTitle());
        shareVo.setAppDesc(properties.getAppDesc());
        shareVo.setUser(user);
        shareVo.setId(share.getId());

        publisher.publishEvent(new ShareEvent(shareVo));

        return Response.success(shareVo);
    }

}

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
package com.apzda.hajimi.weixin.ma.handler;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaMessage;
import com.apzda.hajimi.weixin.http.WxCommonRequestWrapper;
import com.apzda.hajimi.weixin.ma.IWxMaNotificationHandler;
import com.apzda.kalami.http.HttpRequestWrapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class WxMaCallbackHandlerFunction implements HandlerFunction<ServerResponse> {

    private final WxMaService wxMaService;

    private final ObjectProvider<IWxMaNotificationHandler> handlerProvider;

    @Override
    @Nonnull
    public ServerResponse handle(@Nonnull ServerRequest request) throws Exception {
        val appId = request.pathVariable("appId");
        log.trace("Received callback of appId : {}", appId);

        try {
            val service = wxMaService.switchoverTo(appId);
            val method = request.method();
            val query = WxCommonRequestWrapper.from(request);
            val timestamp = query.getTimestamp();
            val nonce = query.getNonce();
            val signature = query.getSignature();
            if (!service.checkSignature(timestamp, nonce, signature)) {
                return ServerResponse.status(403).body("Invalid Signature!");
            }

            if (method == HttpMethod.GET) {
                val echoStr = query.getEchoStr();
                if (StringUtils.isNotBlank(echoStr)) {
                    return ServerResponse.ok().body(echoStr);
                }
                else {
                    return ServerResponse.status(403).body("echostr is blank!");
                }
            }
            else if (method == HttpMethod.POST) {
                val openid = query.getOpenid();
                val encryptType = query.getEncryptType();
                val msgSignature = query.getMsgSignature();
                val wrapper = HttpRequestWrapper.from(request);
                val msgType = wxMaService.getWxMaConfig().getMsgDataFormat();

                final String requestBody;
                try {
                    requestBody = wrapper.getRequestBody();
                }
                catch (IOException e) {
                    log.error("Can't get callback body for {} - {}", appId, e.getMessage());
                    return ServerResponse.status(500).build();
                }

                log.trace("""
                         接收微信请求[{}]：[openid=[{}], [signature=[{}], encType=[{}], msgSignature=[{}],\
                         timestamp=[{}], nonce=[{}], requestBody=[
                        {}
                        ]""", appId, openid, signature, encryptType, msgSignature, timestamp, nonce, requestBody);

                final WxMaMessage inMessage;
                if (encryptType == null) {
                    if (msgType.equals("XML")) {
                        inMessage = WxMaMessage.fromXml(requestBody);
                    }
                    else {
                        inMessage = WxMaMessage.fromJson(requestBody);
                    }
                    if (log.isTraceEnabled()) {
                        log.trace("\n消息内容为[{}]: \n{} ", appId, inMessage.toString());
                    }
                }
                else if ("aes".equalsIgnoreCase(encryptType)) {
                    // aes加密的消息
                    if (msgType.equals("XML")) {
                        inMessage = WxMaMessage.fromEncryptedXml(requestBody, wxMaService.getWxMaConfig(), timestamp,
                                nonce, msgSignature);
                    }
                    else {
                        inMessage = WxMaMessage.fromEncryptedJson(requestBody, wxMaService.getWxMaConfig());
                    }

                    if (log.isTraceEnabled()) {
                        log.trace("\n消息解密后内容为[{}]：\n{} ", appId, inMessage.toString());
                    }
                }
                else {
                    return ServerResponse.status(HttpStatus.NO_CONTENT).body("");
                }

                val result = notify(appId, inMessage, requestBody, request);

                return Objects.requireNonNullElseGet(result, () -> ServerResponse.ok().body("success"));
            }

            return ServerResponse.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }
        catch (Exception e) {
            log.error("Cannot handle: {}\nparams={}", request.uri(), request.params(), e);
            return ServerResponse.status(404).build();
        }
    }

    @Nullable
    protected ServerResponse notify(String appId, @Nullable WxMaMessage response, String body,
            @Nonnull ServerRequest request) {
        val handlers = handlerProvider.orderedStream().toList();

        ServerResponse resp = null;

        if (!CollectionUtils.isEmpty(handlers)) {
            for (IWxMaNotificationHandler handler : handlers) {
                try {
                    val res = handler.notify(appId, response, body, request);
                    if (res != null) {
                        resp = res;
                    }
                }
                catch (Exception e) {
                    log.error("Cannot notify handler: {}\nappId={}\nbody={}", handler.toString(), appId, body, e);
                }
            }
        }

        return resp;
    }

}

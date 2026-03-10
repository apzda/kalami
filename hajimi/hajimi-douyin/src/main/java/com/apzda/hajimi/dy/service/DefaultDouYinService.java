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
package com.apzda.hajimi.dy.service;

import com.apzda.hajimi.dy.autoconfig.DouYinConfigProperties;
import com.apzda.hajimi.dy.openapi.Client;
import com.apzda.hajimi.dy.openapi.request.OauthClientTokenRequest;
import com.apzda.hajimi.dy.openapi.request.OpenGetTicketRequest;
import com.apzda.hajimi.dy.token.DouYinToken;
import com.apzda.kalami.exception.BizException;
import com.apzda.kalami.service.DistributedLockService;
import com.apzda.kalami.service.TempStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultDouYinService implements DouYinService {

    private final static String CLIENT_TOKEN_KEY = "dy.client.token";

    private final static String CLIENT_TOKEN_KEY_LOCK = "dy.client.lock";

    private final static String OPEN_TICKET_KEY = "dy.open.ticket";

    private final static String OPEN_TICKET_LOCK = "dy.ticket.lock";

    private final Client client;

    private final DouYinConfigProperties properties;

    private final TempStorageService storageService;

    private final DistributedLockService distributedLockService;

    @Override
    public String getClientToken() {
        val token = storageService.load(CLIENT_TOKEN_KEY, DouYinToken.class);
        if (token.isPresent()) {
            return token.get().getToken();
        }
        val lock = distributedLockService.getLock(CLIENT_TOKEN_KEY_LOCK);
        try {
            lock.lock();
            val req = new OauthClientTokenRequest();
            req.setClientKey(properties.getClientKey());
            req.setClientSecret(properties.getClientSecret());
            req.setGrantType("client_credential");

            val resp = client.oauthClientToken(req);
            val data = resp.getData();
            val dyToken = data.getAccessToken();
            val expired = data.getExpiresIn() - 30;

            try {
                val ct = new DouYinToken();
                ct.setToken(dyToken);
                ct.setExpireTime(Duration.ofSeconds(expired));
                storageService.save(CLIENT_TOKEN_KEY, ct);
            }
            catch (Exception e) {
                log.warn("缓存client_token失败: {}", e.getMessage());
            }

            return dyToken;
        }
        catch (Exception e) {
            throw new BizException("无法获取抖音令牌", e);
        }
        finally {
            lock.unlock();
            distributedLockService.deleteLock(CLIENT_TOKEN_KEY_LOCK);
        }
    }

    @Override
    public String getAccessToken() {
        return "";
    }

    @Override
    public String getOpenTicket() {
        val token = storageService.load(OPEN_TICKET_KEY, DouYinToken.class);
        if (token.isPresent()) {
            return token.get().getToken();
        }
        val lock = distributedLockService.getLock(OPEN_TICKET_LOCK);
        try {
            lock.lock();
            OpenGetTicketRequest request = new OpenGetTicketRequest();
            request.setAccessToken(this.getClientToken());
            val response = client.openGetTicket(request);
            val data = response.getData();
            val ticket = data.getTicket();
            val expired = data.getExpiresIn() - 30;

            try {
                val dyt = new DouYinToken();
                dyt.setToken(ticket);
                dyt.setExpireTime(Duration.ofSeconds(expired));
                storageService.save(OPEN_TICKET_KEY, dyt);
            }
            catch (Exception e) {
                log.warn("缓存open_ticket失败：{}", e.getMessage());
            }

            return ticket;
        }
        catch (Exception e) {
            throw new BizException("无法获取抖音分享票据", e);
        }
        finally {
            lock.unlock();
            distributedLockService.deleteLock(OPEN_TICKET_LOCK);
        }
    }

}

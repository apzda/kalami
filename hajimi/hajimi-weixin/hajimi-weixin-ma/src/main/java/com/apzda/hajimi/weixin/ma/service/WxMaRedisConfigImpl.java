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
package com.apzda.hajimi.weixin.ma.service;

import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import jakarta.annotation.Nonnull;
import lombok.val;
import me.chanjar.weixin.common.enums.TicketType;
import me.chanjar.weixin.common.redis.WxRedisOps;

import java.util.concurrent.TimeUnit;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
class WxMaRedisConfigImpl extends WxMaDefaultConfigImpl {

    private static final String ACCESS_TOKEN_KEY_TPL = "%s:access_token:%s";

    private static final String TICKET_KEY_TPL = "%s:ticket:key:%s:%s";

    private static final String LOCK_KEY_TPL = "%s:lock:%s:";

    private final WxRedisOps redisOps;

    private final String keyPrefix;

    private String accessTokenKey;

    public WxMaRedisConfigImpl(WxRedisOps redisOps, String keyPrefix) {
        this.redisOps = redisOps;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void setAppid(String appId) {
        super.setAppid(appId);
        this.accessTokenKey = String.format(ACCESS_TOKEN_KEY_TPL, this.keyPrefix, appId);
        val lockKey = String.format(LOCK_KEY_TPL, this.keyPrefix, appId);
        accessTokenLock = this.redisOps.getLock(lockKey.concat("accessTokenLock"));
        jsapiTicketLock = this.redisOps.getLock(lockKey.concat("jsapiTicketLock"));
        cardApiTicketLock = this.redisOps.getLock(lockKey.concat("cardApiTicketLock"));
    }

    @Override
    public String getAccessToken() {
        return redisOps.getValue(this.accessTokenKey);
    }

    @Override
    public boolean isAccessTokenExpired() {
        Long expire = redisOps.getExpire(this.accessTokenKey);
        return expire == null || expire < 2;
    }

    @Override
    public synchronized void updateAccessToken(String accessToken, int expiresInSeconds) {
        redisOps.setValue(this.accessTokenKey, accessToken, expiresInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void expireAccessToken() {
        redisOps.expire(this.accessTokenKey, 0, TimeUnit.SECONDS);
    }

    @Override
    public String getJsapiTicket() {
        return redisOps.getValue(this.getTicketRedisKey(TicketType.JSAPI));
    }

    @Override
    public boolean isJsapiTicketExpired() {
        return redisOps.getExpire(this.getTicketRedisKey(TicketType.JSAPI)) < 2;
    }

    @Override
    public synchronized void updateJsapiTicket(String jsapiTicket, int expiresInSeconds) {
        redisOps.setValue(this.getTicketRedisKey(TicketType.JSAPI), jsapiTicket, expiresInSeconds - 200,
                TimeUnit.SECONDS);
    }

    @Override
    public void expireJsapiTicket() {
        redisOps.expire(this.getTicketRedisKey(TicketType.JSAPI), 0, TimeUnit.SECONDS);
    }

    @Override
    public boolean isCardApiTicketExpired() {
        return redisOps.getExpire(this.getTicketRedisKey(TicketType.WX_CARD)) < 2;
    }

    @Override
    public String getCardApiTicket() {
        return redisOps.getValue(this.getTicketRedisKey(TicketType.WX_CARD));
    }

    @Override
    public void expireCardApiTicket() {
        redisOps.expire(this.getTicketRedisKey(TicketType.WX_CARD), 0, TimeUnit.SECONDS);
    }

    @Override
    public void updateCardApiTicket(String cardApiTicket, int expiresInSeconds) {
        redisOps.setValue(this.getTicketRedisKey(TicketType.WX_CARD), cardApiTicket, expiresInSeconds - 200,
                TimeUnit.SECONDS);
    }

    @Nonnull
    private String getTicketRedisKey(@Nonnull TicketType type) {
        return String.format(TICKET_KEY_TPL, this.keyPrefix, appid, type.getCode());
    }

}

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
package com.apzda.kalami.common.openapi.token;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import cn.hutool.jwt.JWTUtil;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

/**
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public class JwtTokenManager implements TokenManager {
    private final byte[] key;
    private final Duration expireDuration;
    private final long leeway;

    public JwtTokenManager(@Nonnull String jwtKey, Duration expireDuration, long leeway) {
        this.key = jwtKey.getBytes();
        this.expireDuration = expireDuration;
        this.leeway = leeway;
    }

    @Override
    public boolean isValid(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        try {
            val jwt = JWTUtil.parseToken(token);
            jwt.setKey(key);

            return jwt.validate(leeway);
        } catch (JWTException e) {
            log.warn("校验TOKEN发生错误，做无效处理： {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getClientId(String token) {
        try {
            return JWTUtil.parseToken(token).getPayload("sub").toString();
        } catch (Exception e) {
            log.warn("解析TOKEN发生错误无法获取clientId： {}", e.getMessage());
            return null;
        }
    }

    @Override
    public <T> String createToken(T data) {
        val token = JWT.create();
        token.setKey(this.key);

        if (data instanceof String strData) {
            token.setSubject(strData);
        } else {
            token.setSubject(data.toString());
        }

        val accessExpireAt = DateUtil.date().offset(DateField.MINUTE, (int) expireDuration.toMinutes());
        token.setExpiresAt(accessExpireAt);

        return token.sign();
    }
}

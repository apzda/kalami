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
package com.apzda.kalami.common.openapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "kalami.openapi")
@Data
public class OpenApiProperties {

    /**
     * 简易模式下不校验签名,不解密请求体
     */
    private boolean simpleMode;

    /**
     * 加密模式
     */
    private EncryptMode encryptMode = EncryptMode.BODY;

    /**
     * 加密算法
     */
    private String algorithm = "AES";

    /**
     * 请求时间偏差（单位秒），请求时间与本地时间超过leeway将视为非法请求
     */
    private long leeway = 30;

    /**
     * 测试客户ID
     */
    private String testClientId;

    /**
     * 平台公钥
     */
    private String serverPublicKey;

    /**
     * 平台私钥
     */
    private String serverPrivateKey;

    /**
     * 用于生成JWT Token
     */
    private String tokenKey = "888888";

    /**
     * Token 有效期
     */
    @DurationUnit(value = ChronoUnit.HOURS)
    private Duration tokenLifeTime = Duration.ofHours(2);

    public enum EncryptMode {

        ALL, BODY, NONE

    }

}

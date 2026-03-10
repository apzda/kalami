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
package com.apzda.hajimi.weixin.mp;

import jakarta.annotation.Nullable;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "hajimi.weixin.mp")
public class WxMpProperties {

    /**
     * 通知回调URL
     */
    private String url;

    /**
     * 通知回调地址
     */
    private String callback = "wxMp";

    /**
     * 是否使用稳定版访问令牌
     */
    private boolean stableAccessToken;

    /**
     * 最大重试次数
     */
    private int maxRetryTimes = 5;

    /**
     * 重试间隔
     */
    private int retrySleepMillis = 1000;

    /**
     * 小程序配置
     */
    private final List<WxMpConfiguration> configs = new ArrayList<>();

    @Nullable
    public String defaultAppId() {
        if (configs.isEmpty()) {
            return null;
        }
        return configs.get(0).getAppid();
    }

    public String theCallbackPath(String upstream) {
        var cb = this.callback;
        if (StringUtils.isNotBlank(cb)) {
            cb = "/" + StringUtils.strip(cb, "/");
        }
        var host = StringUtils.stripEnd(url, "/");

        return String.format("%s%s/%s/", host, cb, upstream);
    }

    public String theCallbackPathPattern() {
        var cb = this.callback;
        if (StringUtils.isNotBlank(cb)) {
            cb = "/" + StringUtils.strip(cb, "/");
        }

        return String.format("%s/{appId:[0-9a-zA-Z_]+}/**", cb);
    }

}

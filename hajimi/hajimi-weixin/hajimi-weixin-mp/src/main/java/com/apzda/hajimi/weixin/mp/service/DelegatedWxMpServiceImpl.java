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
package com.apzda.hajimi.weixin.mp.service;

import com.apzda.hajimi.weixin.mp.WxMpConfigRegistration;
import com.apzda.hajimi.weixin.mp.WxMpConfiguration;
import com.apzda.hajimi.weixin.mp.WxMpProperties;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceHttpClientImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import me.chanjar.weixin.mp.config.impl.WxMpRedisConfigImpl;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedWxMpServiceImpl extends WxMpServiceHttpClientImpl {

    private final WxRedisOps wxRedisOps;

    private final WxMpProperties properties;

    private final ObjectProvider<WxMpConfigRegistration> registrations;

    @Override
    public WxMpService switchoverTo(String miniAppId) {
        return this.switchoverTo(miniAppId, (id) -> {
            WxMpConfiguration config = registrations.orderedStream().map((registration) -> {
                try {
                    return registration.loadConfig(miniAppId);
                }
                catch (Exception e) {
                    log.warn("""
                            加载公众号配置过程中出错:
                            【appId】   {}
                            【注册中心】{}
                            【异常提示】{}
                            """, miniAppId, registration.getClass(), e.getMessage());
                    return null;
                }
            }).filter(Objects::nonNull).findFirst().orElse(null);

            if (config == null) {
                throw new IllegalStateException(String.format("公众号【%s】未配置", miniAppId));
            }

            val wxMaConfig = createWxMpConfig(config);

            wxMaConfig.setMaxRetryTimes(properties.getMaxRetryTimes());
            wxMaConfig.setRetrySleepMillis(properties.getRetrySleepMillis());
            wxMaConfig.useStableAccessToken(properties.isStableAccessToken());

            return wxMaConfig;
        });
    }

    @Override
    public boolean switchover(String miniAppId) {
        try {
            this.switchoverTo(miniAppId);
            return true;
        }
        catch (Exception e) {
            log.error("切换到公众号[{}]失败: {}", miniAppId, e.getMessage());
            return false;
        }
    }

    @Nonnull
    private WxMpDefaultConfigImpl createWxMpConfig(@Nonnull WxMpConfiguration config) {
        final WxMpDefaultConfigImpl conf;
        if (wxRedisOps == null) {
            conf = new WxMpDefaultConfigImpl();
        }
        else {
            conf = new WxMpRedisConfigImpl(wxRedisOps, "wxMp");
        }

        conf.setAppId(config.getAppid());
        conf.setSecret(config.getSecret());
        conf.setToken(config.getToken());
        conf.setAesKey(config.getAesKey());
        return conf;
    }

}

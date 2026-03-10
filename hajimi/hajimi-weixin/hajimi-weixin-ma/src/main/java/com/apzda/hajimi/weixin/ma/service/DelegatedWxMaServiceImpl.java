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

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceHttpClientImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.apzda.hajimi.weixin.ma.WxMaConfigRegistration;
import com.apzda.hajimi.weixin.ma.WxMaConfiguration;
import com.apzda.hajimi.weixin.ma.WxMaProperties;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.chanjar.weixin.common.redis.WxRedisOps;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DelegatedWxMaServiceImpl extends WxMaServiceHttpClientImpl {

    private final WxRedisOps wxRedisOps;

    private final WxMaProperties properties;

    private final ObjectProvider<WxMaConfigRegistration> registrations;

    @Override
    @SuppressWarnings("unchecked")
    public WxMaService switchoverTo(String miniAppId) {
        return this.switchoverTo(miniAppId, (id) -> {
            WxMaConfiguration config = registrations.orderedStream().map((registration) -> {
                try {
                    return registration.loadConfig(miniAppId);
                }
                catch (Exception e) {
                    log.warn("""
                            加载小程序配置过程中出错:
                            【appId】   {}
                            【注册中心】{}
                            【异常提示】{}
                            """, miniAppId, registration.getClass(), e.getMessage());
                    return null;
                }
            }).filter(Objects::nonNull).findFirst().orElse(null);

            if (config == null) {
                throw new IllegalStateException(String.format("小程序【%s】未配置", miniAppId));
            }

            val wxMaConfig = createWxMaConfig(config);
            wxMaConfig.setMsgDataFormat(properties.getMsgDataFormat().name());
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
            log.error("切换到小程序[{}]失败: {}", miniAppId, e.getMessage());
            return false;
        }
    }

    @Nonnull
    private WxMaDefaultConfigImpl createWxMaConfig(@Nonnull WxMaConfiguration config) {
        final WxMaDefaultConfigImpl conf;
        if (wxRedisOps == null) {
            conf = new WxMaDefaultConfigImpl();
        }
        else {
            conf = new WxMaRedisConfigImpl(wxRedisOps, "wxMa");
        }

        conf.setAppid(config.getAppid());
        conf.setSecret(config.getSecret());
        conf.setToken(config.getToken());
        conf.setAesKey(config.getAesKey());
        return conf;
    }

}

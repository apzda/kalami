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
package com.apzda.hajimi.weixin.mp.registration;

import com.apzda.hajimi.weixin.mp.WxMpConfigRegistration;
import com.apzda.hajimi.weixin.mp.WxMpConfiguration;
import com.apzda.hajimi.weixin.mp.WxMpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultMpConfigRegistration implements WxMpConfigRegistration {

    private final WxMpProperties properties;

    @Override
    public WxMpConfiguration loadConfig(String appId) {
        if (CollectionUtils.isEmpty(properties.getConfigs())) {
            return null;
        }

        WxMpConfiguration config;
        if ("default".equalsIgnoreCase(appId) || StringUtils.isBlank(appId)) {
            config = properties.getConfigs().get(0);
        }
        else {
            val cfg = properties.getConfigs().stream().filter((c) -> appId.equals(c.getAppid())).findFirst();
            config = cfg.orElse(null);
        }

        return config;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}

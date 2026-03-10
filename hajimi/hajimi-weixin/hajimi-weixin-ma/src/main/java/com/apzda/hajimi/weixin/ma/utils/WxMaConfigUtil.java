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
package com.apzda.hajimi.weixin.ma.utils;

import com.apzda.hajimi.weixin.ma.WxMaConfiguration;
import com.apzda.hajimi.weixin.ma.WxMaConfigRegistration;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;

import java.util.Map;
import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public abstract class WxMaConfigUtil {

    private static Lazy<ObjectProvider<WxMaConfigRegistration>> loader;

    public WxMaConfigUtil(ApplicationContext applicationContext) {
        loader = Lazy.of(() -> applicationContext.getBeanProvider(WxMaConfigRegistration.class));
    }

    public static WxMaConfiguration getWxMaConfig(String appId) {
        return loader.get()
            .orderedStream()
            .map(rg -> rg.loadConfig(appId))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Nonnull
    public static Map<String, String> templates(String appId) {
        return loader.get()
            .orderedStream()
            .map(rg -> rg.loadConfig(appId))
            .filter(Objects::nonNull)
            .findFirst()
            .map(WxMaConfiguration::getTemplates)
            .orElse(Map.of());
    }

}

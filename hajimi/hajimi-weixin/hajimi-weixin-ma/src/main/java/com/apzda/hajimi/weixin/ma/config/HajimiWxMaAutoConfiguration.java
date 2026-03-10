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
package com.apzda.hajimi.weixin.ma.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.apzda.hajimi.weixin.ma.IWxMaNotificationHandler;
import com.apzda.hajimi.weixin.ma.WxMaConfigRegistration;
import com.apzda.hajimi.weixin.ma.WxMaProperties;
import com.apzda.hajimi.weixin.ma.handler.WxMaCallbackHandlerFunction;
import com.apzda.hajimi.weixin.ma.registration.DefaultMaConfigRegistration;
import com.apzda.hajimi.weixin.ma.service.DelegatedWxMaServiceImpl;
import com.apzda.hajimi.weixin.ma.utils.WxMaConfigUtil;
import lombok.val;
import me.chanjar.weixin.common.redis.WxRedisOps;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ WxMaProperties.class })
public class HajimiWxMaAutoConfiguration {

    public HajimiWxMaAutoConfiguration(ApplicationContext context) {
        new WxMaConfigUtil(context) {
        };
    }

    @Bean
    WxMaConfigRegistration defaultMaConfigRegistration(WxMaProperties properties) {
        return new DefaultMaConfigRegistration(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    WxMaService hajimiWxMaService(ObjectProvider<WxMaConfigRegistration> registrations, WxMaProperties properties,
            @Autowired(required = false) WxRedisOps wxRedisOps) {
        return new DelegatedWxMaServiceImpl(wxRedisOps, properties, registrations);
    }

    @Bean
    @ConditionalOnProperty(prefix = "hajimi.weixin.ma", name = "url")
    public RouterFunction<ServerResponse> wxMaCallbackRouterFunction(WxMaService wxMaService, WxMaProperties properties,
            ObjectProvider<IWxMaNotificationHandler> handlerProvider) {
        val func = new WxMaCallbackHandlerFunction(wxMaService, handlerProvider);

        return RouterFunctions.route().path(properties.theCallbackPathPattern(), builder -> {
            builder.GET(func).POST(func).build();
        }).build();
    };

}

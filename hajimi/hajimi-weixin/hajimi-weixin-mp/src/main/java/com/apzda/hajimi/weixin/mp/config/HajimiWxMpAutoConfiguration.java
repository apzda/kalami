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
package com.apzda.hajimi.weixin.mp.config;

import com.apzda.hajimi.weixin.mp.IWxMpNotificationHandler;
import com.apzda.hajimi.weixin.mp.WxMpConfigRegistration;
import com.apzda.hajimi.weixin.mp.WxMpProperties;
import com.apzda.hajimi.weixin.mp.handler.WxMpCallbackHandlerFunction;
import com.apzda.hajimi.weixin.mp.registration.DefaultMpConfigRegistration;
import com.apzda.hajimi.weixin.mp.service.DelegatedWxMpServiceImpl;
import com.apzda.hajimi.weixin.mp.utils.WxMpConfigUtil;
import lombok.val;
import me.chanjar.weixin.common.redis.WxRedisOps;
import me.chanjar.weixin.mp.api.WxMpService;
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
@EnableConfigurationProperties({ WxMpProperties.class })
public class HajimiWxMpAutoConfiguration {

    public HajimiWxMpAutoConfiguration(ApplicationContext context) {
        new WxMpConfigUtil(context) {
        };
    }

    @Bean
    WxMpConfigRegistration defaultMpConfigRegistration(WxMpProperties properties) {
        return new DefaultMpConfigRegistration(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    WxMpService hajimiWxMpService(ObjectProvider<WxMpConfigRegistration> registrations, WxMpProperties properties,
            @Autowired(required = false) WxRedisOps wxRedisOps) {
        return new DelegatedWxMpServiceImpl(wxRedisOps, properties, registrations);
    }

    @Bean
    @ConditionalOnProperty(prefix = "hajimi.weixin.mp", name = "url")
    public RouterFunction<ServerResponse> wxMpCallbackRouterFunction(WxMpService wxMaService, WxMpProperties properties,
            ObjectProvider<IWxMpNotificationHandler> handlerProvider) {
        val func = new WxMpCallbackHandlerFunction(wxMaService, handlerProvider);

        return RouterFunctions.route().path(properties.theCallbackPathPattern(), builder -> {
            builder.GET(func).POST(func).build();
        }).build();
    };

}

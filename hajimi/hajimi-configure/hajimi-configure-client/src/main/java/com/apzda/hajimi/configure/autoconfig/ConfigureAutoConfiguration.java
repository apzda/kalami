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
package com.apzda.hajimi.configure.autoconfig;

import com.apzda.hajimi.configure.client.ConfigureService;
import com.apzda.hajimi.configure.event.SettingChangedEvent;
import com.apzda.hajimi.configure.listener.SettingChangedListener;
import com.apzda.hajimi.configure.service.SettingService;
import com.apzda.hajimi.configure.service.impl.SettingServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static com.apzda.hajimi.configure.listener.SettingChangedListener.BEAN_NAME;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
@AutoConfiguration(after = RedisAutoConfiguration.class)
@Import(RedisConfiguration.class)
@EnableConfigurationProperties(LabelConfigProperties.class)
public class ConfigureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    SettingService configureSettingService(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        return new SettingServiceImpl(objectMapper, applicationContext);
    }

    @Bean(BEAN_NAME)
    @ConditionalOnMissingBean(name = BEAN_NAME)
    ApplicationListener<SettingChangedEvent> configSettingChangedListener(SettingService settingService) {
        log.debug("Local Setting Changed Listener configured!");
        return new SettingChangedListener(settingService);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "feign.slf4j.Slf4jLogger")
    @EnableFeignClients(clients = { ConfigureService.class, })
    static class ConfigureOpenfeignClientConfiguration {

    }

}

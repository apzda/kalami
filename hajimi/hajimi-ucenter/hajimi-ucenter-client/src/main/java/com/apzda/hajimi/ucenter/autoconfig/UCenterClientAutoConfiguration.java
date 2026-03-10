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
package com.apzda.hajimi.ucenter.autoconfig;

import com.apzda.hajimi.ucenter.config.UCenterSecurityConfigureProperties;
import com.apzda.hajimi.ucenter.config.UCenterTenantConfigureProperties;
import com.apzda.hajimi.ucenter.security.UCenterMetaUserDetailsService;
import com.apzda.hajimi.ucenter.security.UCenterTenantManager;
import com.apzda.hajimi.ucenter.service.UCenterService;
import com.apzda.hajimi.ucenter.service.UserAccountService;
import com.apzda.hajimi.ucenter.service.impl.UCenterUserInfoServiceImpl;
import com.apzda.kalami.security.autoconfig.KalamiSecurityAutoConfiguration;
import com.apzda.kalami.security.user.MetaUserDetailsService;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Lazy;

import java.util.concurrent.CompletableFuture;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({ KalamiSecurityAutoConfiguration.class })
@ConditionalOnClass(KalamiSecurityAutoConfiguration.class)
@EnableConfigurationProperties({ UCenterSecurityConfigureProperties.class, UCenterTenantConfigureProperties.class })
public class UCenterClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    MetaUserDetailsService ucenterMetaUserDetailsService(ApplicationContext applicationContext) {
        return new UCenterMetaUserDetailsService(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    UserInfoService userInfoService(UCenterService ucenterService) {
        return new UCenterUserInfoServiceImpl(ucenterService);
    }

    @Bean
    @ConditionalOnMissingBean
    TenantManager tenantManager(ApplicationContext context, UCenterTenantConfigureProperties properties) {
        return new UCenterTenantManager(Lazy.of(() -> context.getBean(UCenterService.class)), properties);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "hajimi.ucenter.security", name = "auto-sync", havingValue = "true",
            matchIfMissing = true)
    @Slf4j
    static class SecurityConfigureSyncer implements SmartLifecycle {

        private final UCenterService uCenterService;

        private final UCenterSecurityConfigureProperties properties;

        private volatile boolean running = false;

        SecurityConfigureSyncer(UCenterService uCenterService, UCenterSecurityConfigureProperties properties) {
            this.uCenterService = uCenterService;
            this.properties = properties;
        }

        @Override
        public void start() {
            if (!running) {
                running = true;
                CompletableFuture.runAsync(() -> {
                    try {
                        if (properties.getResources().isEmpty() && properties.getRoles().isEmpty()
                                && properties.getPrivileges().isEmpty()) {
                            return;
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Starting sync security configuration:  {}", properties);
                        }

                        val response = uCenterService.sync(properties);
                        if (!response.isSuccess()) {
                            throw new Exception(response.getErrMsg());
                        }
                        else if (log.isDebugEnabled()) {
                            log.debug("Sync security configuration completed");
                        }
                    }
                    catch (Exception e) {
                        log.warn("Cannot sync security configuration to ucenter server: {}", e.getMessage());
                    }
                });
            }
        }

        @Override
        public void stop() {
            running = false;
        }

        @Override
        public boolean isRunning() {
            return running;
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "feign.slf4j.Slf4jLogger", value = { FallbackFactory.class })
    @EnableFeignClients(clients = { UCenterService.class, UserAccountService.class })
    static class UCenterOpenfeignClientConfiguration {

    }

}

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
package com.apzda.hajimi.sms.service.config;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.apzda.hajimi.sms.core.SmsProvider;
import com.apzda.hajimi.sms.core.SmsSender;
import com.apzda.hajimi.sms.core.config.ProviderProperties;
import com.apzda.hajimi.sms.service.domain.repository.SmsLogRepository;
import com.apzda.hajimi.sms.service.listener.SmsEventListener;
import com.apzda.hajimi.sms.service.provider.TestProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
@Getter
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SmsConfigProperties.class)
@RequiredArgsConstructor
@ComponentScan({ "com.apzda.hajimi.sms.service", "com.apzda.hajimi.sms.controller" })
@MapperScan("com.apzda.hajimi.sms.service.domain.mapper")
public class SmsServiceConfiguration implements InitializingBean, SmartLifecycle {

    private final SmsConfigProperties properties;

    private final Map<String, SmsProvider> enabledSmsProviders = new HashMap<>();

    private final Map<String, ProviderProperties> providerProperties = new HashMap<>();

    private SmsSender<?> smsSender;

    private SmsProvider smsProvider;

    private volatile boolean running = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        val sender = properties.getSender();
        if (StringUtils.isBlank(sender)) {
            log.warn("Sms Sender not specified!");
        }

        val providers = properties.getProviders();

        if (providers.isEmpty()) {
            val tpp = new ProviderProperties();
            tpp.setId("test");
            tpp.setEnabled(true);
            tpp.setTestMode(true);
            providers.add(tpp);
            log.warn("No Sms Provider specified! Using default provider: {}", tpp);
        }

        val enabledProviders = providers.stream().filter(ProviderProperties::isEnabled).peek(ep -> {
            providerProperties.put(ep.getId(), ep);
        }).toList();

        if (enabledProviders.isEmpty()) {
            log.warn("All Sms Provider are disabled!");
        }

        val defaultProvider = StringUtils.defaultIfBlank(properties.getDefaultProvider(),
                enabledProviders.get(0).getId());

        // 初始化服务商
        val smsProviders = ServiceLoaderUtil.loadList(SmsProvider.class, this.getClass().getClassLoader());

        for (SmsProvider provider : smsProviders) {
            val pid = provider.getId();
            if (providerProperties.containsKey(pid)) {
                val pp = providerProperties.get(pid);
                if (properties.isTestMode()) {
                    pp.setTestMode(true);
                }
                provider.init(pp);
                this.enabledSmsProviders.put(pid, provider);
            }

            if (defaultProvider.equals(pid)) {
                this.smsProvider = provider;
                this.enabledSmsProviders.put("default", smsProvider);
            }
        }

        if (this.smsProvider == null) {
            this.smsProvider = new TestProvider();
            this.enabledSmsProviders.put("default", this.smsProvider);
        }

        // 初始化发送器
        val smsSenders = ServiceLoaderUtil.loadList(SmsSender.class, this.getClass().getClassLoader());
        if (smsSenders.isEmpty()) {
            log.warn("no Sms Sender found in class path!");
        }
        val optionalSmsSender = smsSenders.stream().filter(s -> sender.equals(s.getId())).findFirst();
        if (optionalSmsSender.isPresent()) {
            smsSender = optionalSmsSender.get();
            smsSender.init(properties.getProps(), enabledSmsProviders);
        }
        else {
            log.warn("Sms Sender '{}' not found in class path!", sender);
        }

    }

    @Bean
    SmsEventListener hajimiSmsEventListener(ObjectProvider<SmsLogRepository> smsLogRepository) {
        return new SmsEventListener(smsLogRepository);
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        smsSender.stop();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

}

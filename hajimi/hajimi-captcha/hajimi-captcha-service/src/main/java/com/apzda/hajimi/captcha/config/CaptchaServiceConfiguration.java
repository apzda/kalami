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
package com.apzda.hajimi.captcha.config;

import cn.hutool.core.util.ServiceLoaderUtil;
import com.apzda.hajimi.captcha.provider.CaptchaProvider;
import com.apzda.hajimi.captcha.storage.CaptchaStorage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CaptchaConfigProperties.class)
@RequiredArgsConstructor
@ComponentScan({ "com.apzda.hajimi.captcha.controller" })
public class CaptchaServiceConfiguration implements InitializingBean {

    private final ApplicationContext applicationContext;

    private final CaptchaConfigProperties properties;

    private final CaptchaStorage captchaStorage;

    @Value("${kalami.captcha.provider:image}")
    private String provider;

    @Getter
    private CaptchaProvider<?> captchaProvider;

    @Override
    public void afterPropertiesSet() throws Exception {
        val captchaProviders = ServiceLoaderUtil.loadList(CaptchaProvider.class, this.getClass().getClassLoader());
        val props = properties.getProps();
        for (CaptchaProvider<?> captchaProvider : captchaProviders) {
            if (provider.equals(captchaProvider.getId())) {
                captchaProvider.init(captchaStorage, props);
                this.captchaProvider = captchaProvider;
                if (captchaProvider instanceof ApplicationContextAware contextAware) {
                    contextAware.setApplicationContext(applicationContext);
                }
                log.info("Use Captcha Provider: {}", captchaProvider.getClass().getCanonicalName());
                break;
            }
        }
    }

}

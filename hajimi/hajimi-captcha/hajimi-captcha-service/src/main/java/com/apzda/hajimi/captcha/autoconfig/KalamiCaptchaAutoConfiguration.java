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
package com.apzda.hajimi.captcha.autoconfig;

import com.apzda.hajimi.captcha.config.CaptchaConfigProperties;
import com.apzda.hajimi.captcha.storage.CaptchaStorage;
import com.apzda.hajimi.captcha.storage.LocalCaptchaStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@Import({ RedisConfiguration.class })
@EnableConfigurationProperties(CaptchaConfigProperties.class)
public class KalamiCaptchaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    CaptchaStorage captchaStorage(CaptchaConfigProperties properties) {
        log.debug("CaptchaStorage class: {}", LocalCaptchaStorage.class.getCanonicalName());
        return new LocalCaptchaStorage(properties.getTimeout().plus(Duration.ofSeconds(3600)));
    }

}

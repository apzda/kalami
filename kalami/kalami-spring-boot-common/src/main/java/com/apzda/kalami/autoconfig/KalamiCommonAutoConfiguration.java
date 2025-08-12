/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.autoconfig;

import com.apzda.kalami.i18n.I18n;
import com.apzda.kalami.properties.KalamiCommonProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/15
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
@EnableConfigurationProperties({ MessageSourceProperties.class, KalamiCommonProperties.class })
public class KalamiCommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    static MessageSource messageSource(MessageSourceProperties properties) {
        if (!CollectionUtils.isEmpty(properties.getBasename())) {
            I18n.messageSource.addBasenames(properties.getBasename().toArray(new String[0]));
        }

        val cacheDuration = properties.getCacheDuration();

        if (cacheDuration != null) {
            I18n.messageSource.setCacheMillis(cacheDuration.toMillis());
        }

        return I18n.messageSource;
    }

}

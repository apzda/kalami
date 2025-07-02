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
package com.apzda.kalami.web.autoconfig;

import com.apzda.kalami.autoconfig.KalamiCommonAutoConfiguration;
import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.i18n.I18n;
import com.apzda.kalami.infra.config.InfraConfigProperties;
import com.apzda.kalami.web.config.KalamiServerProperties;
import com.apzda.kalami.web.filter.KalamiServletFilter;
import com.apzda.kalami.web.i18n.LocaleResolverImpl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 2025/05/14
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({ WebMvcAutoConfiguration.class, ErrorMvcAutoConfiguration.class,
        WebSocketServletAutoConfiguration.class })
@AutoConfigureAfter(KalamiCommonAutoConfiguration.class)
@Import({ TracingRestClientConfiguration.class, KalamiWebMvcConfigure.class, KalamiUndertowConfig.class,
        KalamiInfraConfiguration.class })
@EnableConfigurationProperties({ WebProperties.class, ServerProperties.class, KalamiServerProperties.class,
        InfraConfigProperties.class })
@RequiredArgsConstructor
public class KalamiWebAutoConfiguration implements WebMvcConfigurer {

    private final WebProperties webProperties;

    @Bean("localeResolver")
    @ConditionalOnMissingBean
    LocaleResolver localeResolver() {
        val locale = Optional.ofNullable(webProperties.getLocale()).orElse(I18n.detectDefaultLocale());
        return new LocaleResolverImpl("lang", locale);
    }

    @Bean
    KalamiContextHolder kalamiContextHolder() {
        return new KalamiContextHolder() {
        };
    }

    @Bean
    FilterRegistrationBean<KalamiServletFilter> gsvcFilterRegistration(LocaleResolver localeResolver) {
        FilterRegistrationBean<KalamiServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new KalamiServletFilter(localeResolver));
        registration.addUrlPatterns("/*");
        registration.setName("kalamiServletFilter");
        registration.setOrder(-104);

        return registration;
    }

}

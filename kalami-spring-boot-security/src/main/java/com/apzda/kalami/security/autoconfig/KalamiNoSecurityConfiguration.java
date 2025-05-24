/*
 * Copyright 2025 the original author or authors.
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

package com.apzda.kalami.security.autoconfig;

import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.token.TokenManager;
import com.apzda.kalami.security.web.filter.KalamiWebSecurityContextFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/21
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ WebMvcConfigurer.class })
@ConditionalOnMissingClass({ "org.springframework.security.config.annotation.web.configuration.EnableWebSecurity",
        "org.springframework.security.web.SecurityFilterChain" })
@RequiredArgsConstructor
class KalamiNoSecurityConfiguration {

    @Bean
    FilterRegistrationBean<KalamiWebSecurityContextFilter> kalamiSecurityContextFilterBean(SecurityConfigProperties properties,
                                                                                           TokenManager tokenManager) {
        FilterRegistrationBean<KalamiWebSecurityContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new KalamiWebSecurityContextFilter(properties, tokenManager));
        registration.addUrlPatterns("/*");
        registration.setName("kalamiSecurityContextFilter");
        registration.setOrder(-103);

        log.info("KalamiSecurityContextFilter initialized for load SecurityContext");
        return registration;
    }

}

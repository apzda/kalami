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

import com.apzda.kalami.security.authorization.checker.AuthorizationChecker;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.reactive.KalamiErrorWebExceptionHandler;
import com.apzda.kalami.security.web.filter.KalamiWebFluxSecurityFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableConfigurationProperties(WebProperties.class)
class KalamiWebFluxConfiguration {

    @Bean
    KalamiErrorWebExceptionHandler kalamiErrorWebExceptionHandler(ErrorAttributes errorAttributes,
            WebProperties properties, ServerCodecConfigurer serverCodecConfigurer,
            ApplicationContext applicationContext) {
        log.trace("KalamiErrorWebExceptionHandler initialized");
        return new KalamiErrorWebExceptionHandler(errorAttributes, properties.getResources(), applicationContext,
                serverCodecConfigurer);
    }

    @Bean
    @Order(0)
    KalamiWebFluxSecurityFilter KalamiWebFluxSecurityFilter(SecurityConfigProperties properties,
            ObjectProvider<AuthorizationChecker> filtersProvider) {
        return new KalamiWebFluxSecurityFilter(properties, filtersProvider);
    }

}

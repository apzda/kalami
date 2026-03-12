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
package com.apzda.hajimi.openapi.autoconfig;

import com.apzda.kalami.common.openapi.config.OpenApiProperties;
import com.apzda.kalami.common.openapi.token.JwtTokenManager;
import com.apzda.kalami.common.openapi.token.TokenManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@AutoConfiguration(before = com.apzda.kalami.common.openapi.autoconfig.OpenApiAutoConfiguration.class)
//@formatter:off
@ComponentScan({
    "com.apzda.hajimi.openapi.domain",
    "com.apzda.hajimi.openapi.provider",
    "com.apzda.hajimi.openapi.controller",
    "com.apzda.hajimi.openapi.openapi",
    "com.apzda.hajimi.openapi.service"
})
//@formatter:on
@MapperScan({ "com.apzda.hajimi.openapi.mapper" })
@EnableConfigurationProperties({ OpenApiProperties.class })
public class OpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    TokenManager tokenManager(OpenApiProperties properties) {
        return new JwtTokenManager(properties.getTokenKey(), properties.getTokenLifeTime(), properties.getLeeway());
    }

}

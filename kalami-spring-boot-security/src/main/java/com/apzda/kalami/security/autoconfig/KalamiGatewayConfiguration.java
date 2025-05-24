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

import com.apzda.kalami.security.authentication.repository.ReactiveJwtContextRepository;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.token.TokenManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.util.CollectionUtils;

/**
 * 配置spring-cloud-gateway
 *
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/19
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ ServerHttpSecurity.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
class KalamiGatewayConfiguration {

    @Bean
    ServerSecurityContextRepository reactiveJwtContextRepository(TokenManager tokenManager,
            SecurityConfigProperties properties) {
        return new ReactiveJwtContextRepository(tokenManager, properties);
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
            ServerSecurityContextRepository securityContextRepository, SecurityConfigProperties properties) {

        http.authorizeExchange((exchange) -> {
            exchange.anyExchange().permitAll();
        });

        http.requestCache(ServerHttpSecurity.RequestCacheSpec::disable);
        http.anonymous(ServerHttpSecurity.AnonymousSpec::disable);
        http.formLogin(ServerHttpSecurity.FormLoginSpec::disable);
        http.logout(ServerHttpSecurity.LogoutSpec::disable);
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);
        // http.sessionManagement();
        // CORS
        if (CollectionUtils.isEmpty(properties.getCors())) {
            http.cors(ServerHttpSecurity.CorsSpec::disable);
        }
        else {
            http.cors(Customizer.withDefaults());
        }
        // 安全头
        val cfg = properties.getHeaders();
        if (cfg != null) {
            http.headers(headers -> {
                if (!cfg.isHsts()) {
                    headers.hsts(ServerHttpSecurity.HeaderSpec.HstsSpec::disable);
                }
                if (!cfg.isXss()) {
                    headers.xssProtection(ServerHttpSecurity.HeaderSpec.XssProtectionSpec::disable);
                }
                if (!cfg.isFrame()) {
                    headers.frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable);
                }
                if (!cfg.isContentType()) {
                    headers.contentTypeOptions(ServerHttpSecurity.HeaderSpec.ContentTypeOptionsSpec::disable);
                }
            });
        }
        else {
            http.headers(ServerHttpSecurity.HeaderSpec::disable);
        }

        http.securityContextRepository(securityContextRepository);
        log.info("KalamiSecurityFilterChain: initialization completed");
        return http.build();
    }

}

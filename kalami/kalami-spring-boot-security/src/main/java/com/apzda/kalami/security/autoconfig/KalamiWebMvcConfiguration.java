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
import com.apzda.kalami.security.web.interceptor.KalamiServletSecurityInterceptor;
import com.apzda.kalami.security.web.resolver.CurrentUserParamResolver;
import com.apzda.kalami.user.CurrentUser;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 用于配置 Spring MVC
 *
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/19
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(WebMvcConfigurer.class)
@RequiredArgsConstructor
class KalamiWebMvcConfiguration {

    private final SecurityConfigProperties properties;

    private final ObjectProvider<AuthorizationChecker> filterProvider;

    @Configuration
    class InitializeKalamiWebMvcConfigurer implements WebMvcConfigurer {

        /**
         * 解析 {@link CurrentUser}参数.
         * @param resolvers initially an empty list
         */
        @Override
        public void addArgumentResolvers(@Nonnull List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new CurrentUserParamResolver());
        }

        /**
         * CORS配置
         */
        @Override
        public void addCorsMappings(@Nonnull CorsRegistry registry) {
            val cors = properties.getCors();
            if (CollectionUtils.isEmpty(cors)) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Add CORS mappings for cors: {}", cors);
            }
            cors.forEach((url, cfg) -> {
                val registration = registry.addMapping(url);

                if (!CollectionUtils.isEmpty(cfg.getOrigins())) {
                    registration.allowedOrigins(cfg.getOrigins().toArray(new String[0]));
                }
                else if (!CollectionUtils.isEmpty(cfg.getOriginPatterns())) {
                    registration.allowedOriginPatterns(cfg.getOriginPatterns().toArray(new String[0]));
                }

                if (!CollectionUtils.isEmpty(cfg.getHeaders())) {
                    registration.allowedHeaders(cfg.getHeaders().toArray(new String[0]));
                }

                if (!CollectionUtils.isEmpty(cfg.getMethods())) {
                    registration.allowedMethods(cfg.getMethods().toArray(new String[0]));
                }

                if (cfg.getMaxAge() != null) {
                    registration.maxAge(cfg.getMaxAge().toSeconds());
                }

                if (cfg.getCredentials() != null) {
                    registration.allowCredentials(cfg.getCredentials());
                }

                if (!CollectionUtils.isEmpty(cfg.getExposed())) {
                    registration.exposedHeaders(cfg.getExposed().toArray(new String[0]));
                }

                if (cfg.getAllowPrivateNetwork() != null) {
                    registration.allowPrivateNetwork(cfg.getAllowPrivateNetwork());
                }
            });
        }

        /**
         * 安全解释器支持类似gateway的route配置
         */
        @Override
        public void addInterceptors(@Nonnull InterceptorRegistry registry) {
            registry.addInterceptor(new KalamiServletSecurityInterceptor(KalamiWebMvcConfiguration.this.properties,
                    KalamiWebMvcConfiguration.this.filterProvider));
        }

    }

}

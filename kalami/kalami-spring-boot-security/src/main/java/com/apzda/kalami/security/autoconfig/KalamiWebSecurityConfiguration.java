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
package com.apzda.kalami.security.autoconfig;

import com.apzda.kalami.security.authentication.AuthenticationUtil;
import com.apzda.kalami.security.authentication.DefaultAuthenticationProvider;
import com.apzda.kalami.security.authentication.filter.AbstractAuthenticatedFilter;
import com.apzda.kalami.security.authentication.filter.AbstractProcessingFilter;
import com.apzda.kalami.security.authentication.filter.AuthenticationExceptionFilter;
import com.apzda.kalami.security.authentication.filter.SecurityFilterRegistrationBean;
import com.apzda.kalami.security.authentication.handler.AuthenticationHandler;
import com.apzda.kalami.security.authentication.handler.DefaultAuthenticationHandler;
import com.apzda.kalami.security.authentication.repository.JwtContextRepository;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.token.JwtTokenCustomizer;
import com.apzda.kalami.security.token.TokenManager;
import com.apzda.kalami.security.web.HttpSecurityCustomizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ EnableWebSecurity.class, WebMvcConfigurer.class, SecurityFilterChain.class })
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableWebSecurity
class KalamiWebSecurityConfiguration {

    private final SecurityConfigProperties properties;

    private final ApplicationEventPublisher eventPublisher;

    private final ObjectProvider<SecurityFilterRegistrationBean<? extends AbstractAuthenticationProcessingFilter>> authenticationFilterProvider;

    private final ObjectProvider<SecurityFilterRegistrationBean<? extends AbstractAuthenticatedFilter>> authenticatedFilterProvider;

    private final ObjectProvider<HttpSecurityCustomizer> httpSecurityCustomizers;

    @Value("${kalami.security.role-prefix:ROLE_}")
    private String rolePrefix = "ROLE_";

    /**
     * 全局认证处理器。
     */
    @Bean
    static KalamiWebSecurityConfigurerAdapter kalamiWebSecurityConfigurerAdapter(ApplicationContext context) {
        return new KalamiWebSecurityConfigurerAdapter(context);
    }

    /**
     * 认证工具类
     */
    @Bean
    static AuthenticationUtil authenticationUtil(AuthenticationManagerBuilder builder,
            SecurityContextRepository securityContextRepository,
            @Autowired(required = false) ApplicationEventPublisher eventPublisher, AuthenticationHandler handler) {
        return new AuthenticationUtil(builder, securityContextRepository, eventPublisher, handler) {
        };
    }

    @Bean("defaultAuthenticationProvider")
    @ConditionalOnMissingBean(AuthenticationProvider.class)
    AuthenticationProvider defaultAuthenticationProvider(PasswordEncoder passwordEncoder) {
        log.debug("Default authentication provider(no one can pass the authentication) used!!!");
        UserDetailsService userDetailsService = new InMemoryUserDetailsManager(List.of());
        return new DefaultAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @Bean
    @ConditionalOnMissingBean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults(rolePrefix);
    }

    /**
     * 认证事件处理器，需要处理以下回调:
     * <p/>
     * <ul>
     * <li>1. AuthenticationFailureHandler</li>
     * <li>2. AuthenticationSuccessHandler</li>
     * <li>3. AccessDeniedHandler</li>
     * <li>4. AuthenticationEntryPoint,</li>
     * <li>5. SessionAuthenticationStrategy,</li>
     * <li>6. InvalidSessionStrategy,</li>
     * <li>7. LogoutHandler,</li>
     * <li>8. LogoutSuccessHandler</li>
     * </ul>
     */
    @Bean
    @ConditionalOnMissingBean
    AuthenticationHandler authenticationHandler(TokenManager tokenManager,
            ObjectProvider<JwtTokenCustomizer> customizers, ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher) {
        return new DefaultAuthenticationHandler(properties, tokenManager, objectMapper, eventPublisher);
    }

    /**
     * 自义存储 Context 仓储
     */
    @Bean
    @ConditionalOnMissingBean
    SecurityContextRepository securityContextRepository(TokenManager jwtTokenManager) {
        return new JwtContextRepository(jwtTokenManager, properties);
    }

    @Bean
    @Order(-100)
    SecurityFilterChain kalamiSecurityFilterChain(HttpSecurity http, AuthenticationManagerBuilder builder,
            SecurityContextRepository securityContextRepository, AuthenticationHandler authenticationHandler,
            ApplicationContext applicationContext) throws Exception {

        log.debug("Initializing SecurityFilterChain 'KalamiSecurityFilterChain(-100)'");
        val stopWatch = new StopWatch();
        stopWatch.start();
        http.servletApi(AbstractHttpConfigurer::disable);
        http.requestCache(AbstractHttpConfigurer::disable);
        // 不需要会话
        http.sessionManagement(AbstractHttpConfigurer::disable);
        http.securityContext((context) -> {
            context.requireExplicitSave(true);
            context.securityContextRepository(securityContextRepository);
        });
        // CORS
        if (CollectionUtils.isEmpty(properties.getCors())) {
            http.cors(AbstractHttpConfigurer::disable);
        }
        else {
            http.cors(Customizer.withDefaults());
        }
        // 安全头
        val cfg = properties.getHeaders();
        if (cfg != null) {
            http.headers(headers -> {
                if (!cfg.isHsts()) {
                    headers.httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable);
                }
                if (!cfg.isXss()) {
                    headers.xssProtection(HeadersConfigurer.XXssConfig::disable);
                }
                if (!cfg.isFrame()) {
                    headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
                }
                if (!cfg.isContentType()) {
                    headers.contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable);
                }
            });
        }
        else {
            http.headers(AbstractHttpConfigurer::disable);
        }
        // 以下禁用前后端分离架构的应用永远不需要的特性
        http.anonymous(AbstractHttpConfigurer::disable);
        http.rememberMe(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.logout(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);

        val filters = authenticationFilterProvider.orderedStream().toList();
        val contextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
        val authenticationManager = builder.getObject();
        for (SecurityFilterRegistrationBean<? extends AbstractAuthenticationProcessingFilter> filterBean : filters) {
            val filter = filterBean.filter();
            // 上下文存储策略
            filter.setSecurityContextHolderStrategy(contextHolderStrategy);
            // 认证管理器
            filter.setAuthenticationManager(authenticationManager);
            // 上下文仓储
            filter.setSecurityContextRepository(securityContextRepository);
            // 认证成功与失败处理器
            filter.setAuthenticationSuccessHandler(authenticationHandler);
            filter.setAuthenticationFailureHandler(authenticationHandler);
            // 认证成功后处理策略（过期，禁止多处登录等）
            filter.setSessionAuthenticationStrategy(authenticationHandler);
            // 认证事件发布者
            filter.setApplicationEventPublisher(eventPublisher);
            // 不允许创建HTTP Session
            filter.setAllowSessionCreation(false);
            // 注入容器上下文
            if (filter instanceof AbstractProcessingFilter processingFilter) {
                processingFilter.setApplicationContext(applicationContext);
            }

            http.addFilterBefore(filter, ExceptionTranslationFilter.class);
            log.debug("AbstractAuthenticationProcessingFilter '{}' configured for use", filter.getClass().getName());
        }
        // 用于处理Session加载过程,CredentialsExpiredFilter,AccountLockedFilter,MfaAuthenticationFilter中的异常
        http.addFilterBefore(new AuthenticationExceptionFilter(authenticationHandler), SessionManagementFilter.class);

        for (SecurityFilterRegistrationBean<? extends AbstractAuthenticatedFilter> filterBean : authenticatedFilterProvider
            .orderedStream()
            .toList()) {
            val filter = filterBean.filter();
            filter.setSecurityContextHolderStrategy(contextHolderStrategy);
            http.addFilterBefore(filter, ExceptionTranslationFilter.class);
            log.debug("AbstractAuthenticatedFilter '{}' configured for use", filter.getClass().getName());
        }

        http.exceptionHandling((exception) -> {
            exception.accessDeniedHandler(authenticationHandler);
            exception.authenticationEntryPoint(authenticationHandler);
        });

        http.authorizeHttpRequests((authorize) -> {
            // 允许所有
            authorize.anyRequest().permitAll();
        });

        for (HttpSecurityCustomizer customizer : httpSecurityCustomizers.orderedStream().toList()) {
            customizer.customize(http);
        }

        log.info("KalamiSecurityFilterChain: initialization completed in {} ms", stopWatch.getTotalTimeMillis());
        return http.build();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({ DefaultAuthenticationEventPublisher.class })
    @ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true")
    static class KalamiTracingConfiguration {

        @Bean
        @ConditionalOnProperty(name = "kalami.security.trace-enabled", havingValue = "false", matchIfMissing = true)
        ObservationRegistryCustomizer<ObservationRegistry> noSpringSecurityObservations() {
            ObservationPredicate predicate = (name, context) -> !name.startsWith("spring.security.");
            return (registry) -> registry.observationConfig().observationPredicate(predicate);
        }

    }

}

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

import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.NoneJWTSigner;
import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.exception.NoStackBizException;
import com.apzda.kalami.http.ExceptionTransformer;
import com.apzda.kalami.security.authorization.AsteriskPermissionEvaluator;
import com.apzda.kalami.security.authorization.AuthorizationLogicCustomizer;
import com.apzda.kalami.security.authorization.PermissionChecker;
import com.apzda.kalami.security.authorization.checker.AuthorizationChecker;
import com.apzda.kalami.security.authorization.checker.HasRoleChecker;
import com.apzda.kalami.security.authorization.checker.MfaAuthorizationChecker;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.context.SpringSecurityAuditorAware;
import com.apzda.kalami.security.context.SpringSecurityUserProvider;
import com.apzda.kalami.security.error.AuthenticationError;
import com.apzda.kalami.security.jackson.SimpleGrantedAuthorityDeserializer;
import com.apzda.kalami.security.token.JWTSignerAdapter;
import com.apzda.kalami.security.token.JwtTokenCustomizer;
import com.apzda.kalami.security.token.JwtTokenManager;
import com.apzda.kalami.security.token.TokenManager;
import com.apzda.kalami.security.user.DefaultMetaUserDetailsService;
import com.apzda.kalami.security.user.MetaUserDetailsService;
import com.apzda.kalami.security.utils.SecurityUtils;
import com.apzda.kalami.user.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.ErrorResponseException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/19
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
//@formatter:off
@AutoConfiguration(
    before = {
        SecurityAutoConfiguration.class,
        RSocketMessagingAutoConfiguration.class,
        ReactiveWebServerFactoryAutoConfiguration.class
    },
    afterName = "com.apzda.kalami.web.autoconfig.KalamiWebAutoConfiguration"
)
@Import({
    KalamiWebSecurityConfiguration.class,
    KalamiReactiveSecurityConfiguration.class,
    KalamiNoSecurityConfiguration.class,
    KalamiWebMvcConfiguration.class,
    KalamiWebFluxConfiguration.class,
    KalamiGatewayConfiguration.class
})
//@formatter:on
@EnableConfigurationProperties({ SecurityConfigProperties.class })
@RequiredArgsConstructor
@ComponentScan({ "com.apzda.kalami.security.aop" })
public class KalamiSecurityAutoConfiguration {

    private final SecurityConfigProperties properties;

    @Value("${kalami.security.role-prefix:ROLE_}")
    private String rolePrefix = "ROLE_";

    @Bean
    @ConditionalOnMissingBean
    static AuditorAware<String> securityAuditor() {
        return new SpringSecurityAuditorAware();
    }

    @Bean
    @ConditionalOnMissingBean
    static PasswordEncoder defaultGsvcPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 认证/授权异常转换器
     */
    @Bean
    static ExceptionTransformer authExceptionTransformer() {
        return new ExceptionTransformer() {
            @Override
            public Exception transform(Throwable exception) {
                if (exception instanceof AccessDeniedException) {
                    return new ErrorResponseException(HttpStatus.FORBIDDEN,
                            new NoStackBizException(ServiceError.FORBIDDEN, exception));
                }
                else if (exception instanceof CredentialsExpiredException) {
                    return new ErrorResponseException(HttpStatus.UNAUTHORIZED,
                            new NoStackBizException(ServiceError.CREDENTIALS_EXPIRED, exception));
                }
                else if (exception instanceof AccountExpiredException) {
                    return new ErrorResponseException(HttpStatus.UNAUTHORIZED,
                            new NoStackBizException(ServiceError.ACCOUNT_EXPIRED, exception));
                }
                else if (exception instanceof LockedException) {
                    return new ErrorResponseException(HttpStatus.UNAUTHORIZED,
                            new NoStackBizException(ServiceError.ACCOUNT_LOCKED, exception));
                }
                else if (exception instanceof DisabledException) {
                    return new ErrorResponseException(HttpStatus.UNAUTHORIZED,
                            new NoStackBizException(ServiceError.ACCOUNT_DISABLED, exception));
                }
                else if (exception instanceof AuthenticationError ae) {
                    val error = ae.getError();
                    return new ErrorResponseException(HttpStatus.UNAUTHORIZED,
                            new NoStackBizException(error, exception));
                }

                return new ErrorResponseException(HttpStatus.UNAUTHORIZED,
                        new NoStackBizException(ServiceError.UNAUTHORIZED, exception));
            }

            @Override
            public boolean supports(Class<? extends Throwable> eClass) {
                return AuthenticationException.class.isAssignableFrom(eClass)
                        || AccessDeniedException.class.isAssignableFrom(eClass);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    static CurrentUserProvider springSecurityCurrentUserProvider() {
        return new SpringSecurityUserProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    PermissionEvaluator asteriskPermissionEvaluator(ObjectProvider<PermissionChecker> checkerProvider) {
        return new AsteriskPermissionEvaluator(checkerProvider);
    }

    @Bean("authz")
    @ConditionalOnMissingBean(name = "authz")
    AuthorizationLogicCustomizer authz(PermissionEvaluator evaluator) {
        return new AuthorizationLogicCustomizer(evaluator);
    }

    @Bean
    @ConditionalOnMissingBean
    MethodSecurityExpressionHandler methodSecurityExpressionHandler(ApplicationContext applicationContext,
            PermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        try {
            val roleHierarchy = applicationContext.getBean(RoleHierarchy.class);
            expressionHandler.setRoleHierarchy(roleHierarchy);
        }
        catch (Exception ignored) {
            log.trace("No RoleHierarchy found");
        }
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        expressionHandler.setDefaultRolePrefix(rolePrefix);
        return expressionHandler;
    }

    @Bean
    @ConditionalOnMissingBean
    SecurityUtils.DefaultSecurityExpressionHandler kalamiSecurityExpressionHandler(
            ApplicationContext applicationContext, PermissionEvaluator permissionEvaluator) {
        RoleHierarchy roleHierarchy = null;
        try {
            roleHierarchy = applicationContext.getBean(RoleHierarchy.class);
        }
        catch (Exception ignored) {
        }

        return new SecurityUtils.DefaultSecurityExpressionHandler(permissionEvaluator, roleHierarchy, rolePrefix);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "kalami.security", name = "jwt-key")
    JWTSigner jwtSigner() {
        return new JWTSignerAdapter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    JWTSigner jwtSignerNone() {
        log.trace("NoneJWTSigner configured!");
        return new NoneJWTSigner() {
            public boolean verify(String headerBase64, String payloadBase64, String signBase64) {
                return !StringUtils.isAnyBlank(headerBase64, payloadBase64);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    MetaUserDetailsService metaUserDetailsFactory() {
        return new DefaultMetaUserDetailsService();
    }

    @Bean
    @ConditionalOnMissingBean
    TokenManager jwtTokenManager(MetaUserDetailsService metaUserDetailsFactory,
            ObjectProvider<JwtTokenCustomizer> customizers, JWTSigner jwtSigner) {
        return new JwtTokenManager(metaUserDetailsFactory, customizers, properties, jwtSigner);
    }

    @Bean("mfaAuthorizationChecker")
    @ConditionalOnMissingBean(name = "mfaAuthorizationChecker")
    AuthorizationChecker mfaAuthorizationChecker() {
        return new MfaAuthorizationChecker();
    }

    @Bean("hasRoleAuthorizationChecker")
    @ConditionalOnMissingBean(name = "hasRoleAuthorizationChecker")
    AuthorizationChecker hasRoleAuthorizationChecker() {
        return new HasRoleChecker();
    }

    @Configuration(proxyBeanMethods = false)
    static class KalamiSecurityObjectMapperConfiguration {

        KalamiSecurityObjectMapperConfiguration(ObjectMapper objectMapper) {
            SimpleModule module = new SimpleModule();
            module.addDeserializer(SimpleGrantedAuthority.class, new SimpleGrantedAuthorityDeserializer());
            objectMapper.registerModule(module);
        }

    }

}

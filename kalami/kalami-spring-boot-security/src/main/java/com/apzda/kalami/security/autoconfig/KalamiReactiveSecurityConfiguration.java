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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import reactor.core.publisher.Mono;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/19
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(
        value = { ReactiveAuthenticationManager.class, ReactiveUserDetailsService.class,
                ReactiveAuthenticationManagerResolver.class },
        type = { "org.springframework.security.oauth2.jwt.ReactiveJwtDecoder" })
@Conditional({ KalamiReactiveSecurityConfiguration.RSocketEnabledOrReactiveWebApplication.class,
        KalamiReactiveSecurityConfiguration.MissingAlternativeOrUserPropertiesConfigured.class })
class KalamiReactiveSecurityConfiguration {

    @Bean
    ReactiveAuthenticationManager authenticationManager() {
        log.trace("KalamiReactiveSecurityConfiguration.authenticationManager()");
        return Mono::just;
    }

    static class RSocketEnabledOrReactiveWebApplication extends AnyNestedCondition {

        RSocketEnabledOrReactiveWebApplication() {
            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnBean(type = "org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler")
        static class RSocketSecurityEnabledCondition {

        }

        @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
        static class ReactiveWebApplicationCondition {

        }

    }

    static final class MissingAlternativeOrUserPropertiesConfigured extends AnyNestedCondition {

        MissingAlternativeOrUserPropertiesConfigured() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnMissingClass({
                "org.springframework.security.oauth2.client.registration.ClientRegistrationRepository",
                "org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector" })
        static final class MissingAlternative {

        }

        @ConditionalOnProperty(prefix = "spring.security.user", name = "name")
        static final class NameConfigured {

        }

        @ConditionalOnProperty(prefix = "spring.security.user", name = "password")
        static final class PasswordConfigured {

        }

    }

}

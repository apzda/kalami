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

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;

import java.util.List;

/**
 * 用于配置全局认证处理器(Global Authentication Manager)
 *
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/19
 * @version 1.0.0
 */
@ConditionalOnClass(GlobalAuthenticationConfigurerAdapter.class)
@Order(KalamiWebSecurityConfigurerAdapter.DEFAULT_ORDER)
class KalamiWebSecurityConfigurerAdapter extends GlobalAuthenticationConfigurerAdapter {

    static final int DEFAULT_ORDER = Ordered.LOWEST_PRECEDENCE - 60000;

    private final ApplicationContext context;

    /**
     * @param context the ApplicationContext to look up beans.
     */
    KalamiWebSecurityConfigurerAdapter(@Nonnull ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void init(@Nonnull AuthenticationManagerBuilder builder) throws Exception {
        builder.apply(new InitializeAuthenticationProviderManagerConfigurer());
    }

    class InitializeAuthenticationProviderManagerConfigurer extends GlobalAuthenticationConfigurerAdapter {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void configure(@Nonnull AuthenticationManagerBuilder builder) {
            if (builder.isConfigured()) {
                return;
            }

            String[] beanNames = KalamiWebSecurityConfigurerAdapter.this.context
                .getBeanNamesForType(AuthenticationProvider.class);
            if (beanNames.length == 0) {
                return;
            }

            ObjectProvider<AuthenticationProvider> authenticationProvider = KalamiWebSecurityConfigurerAdapter.this.context
                .getBeanProvider(AuthenticationProvider.class);
            authenticationProvider.orderedStream().forEach(builder::authenticationProvider);

            this.logger.info("Global AuthenticationManager configured with AuthenticationProviders bean with names {}",
                    List.of(beanNames));
        }

    }

}

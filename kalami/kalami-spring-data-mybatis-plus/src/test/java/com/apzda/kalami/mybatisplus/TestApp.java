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

package com.apzda.kalami.mybatisplus;

import com.apzda.kalami.mybatisplus.config.KalamiMybatisPlusConfigProperties;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUser;
import com.apzda.kalami.user.CurrentUserProvider;
import jakarta.annotation.Nonnull;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan({ "com.apzda.kalami.mybatisplus.service", "com.apzda.kalami.mybatisplus.config" })
@MapperScan("com.apzda.kalami.mybatisplus.mapper")
@EnableConfigurationProperties(KalamiMybatisPlusConfigProperties.class)
public class TestApp {

    @Bean
    TenantManager tenantManager(KalamiMybatisPlusConfigProperties properties) {
        return new TenantManager() {
            @Override
            @Nonnull
            protected String[] getTenantIds() {
                return new String[] { "123456789" };
            }

            @Override
            @Nonnull
            public String getTenantIdColumn() {
                return properties.getTenantIdColumn();
            }

            @Override
            public boolean disableTenantPlugin() {
                return properties.isDisableTenantPlugin();
            }
        };
    }

    @Bean
    CurrentUserProvider currentUserProvider() {
        return new CurrentUserProvider() {
            @Override
            protected CurrentUser currentUser() {
                return CurrentUser.builder().uid("1").build();
            }
        };
    }

}

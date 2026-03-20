/*
 * Copyright 2023-2026 the original author or authors.
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
package com.apzda.kalami.mybatisplus.config;

import com.apzda.kalami.mybatisplus.MybatisPlusConfigureCustomizer;
import jakarta.annotation.Nonnull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Set;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@TestConfiguration(proxyBeanMethods = false)
public class MyBatisPlusConfig {

    @Bean
    MybatisPlusConfigureCustomizer mybatisCustomizer() {
        return new MybatisPlusConfigureCustomizer() {
            @Override
            public void addLocation(@Nonnull Set<String> locations) {
                locations.add("classpath*:/com/apzda/**/*Mapper.xml");
            }

            @Override
            public void addTypeHandlersPackage(@Nonnull Set<String> packages) {
                packages.add("com.apzda.kalami.mybatisplus.handler");
            }

            @Override
            public void addTenantIgnoreTable(@Nonnull Set<String> tables) {
                tables.add("t_roles");
            }
        };
    }

    @Bean
    MybatisPlusConfigureCustomizer mybatisCustomizer1() {
        return new MybatisPlusConfigureCustomizer() {

            @Override
            public void addTypeHandlersPackage(@Nonnull Set<String> packages) {
                packages.add("com.apzda.kalami.mybatisplus.handler1");
            }
        };
    }

}

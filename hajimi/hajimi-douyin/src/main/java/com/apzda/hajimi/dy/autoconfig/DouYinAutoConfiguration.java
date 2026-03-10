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
package com.apzda.hajimi.dy.autoconfig;

import com.apzda.hajimi.dy.openapi.Client;
import com.apzda.hajimi.dy.openapi.Config;
import lombok.val;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(DouYinConfigProperties.class)
@ComponentScan({ "com.apzda.hajimi.dy.controller", "com.apzda.hajimi.dy.service" })
public class DouYinAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Client douYinClient(DouYinConfigProperties props) throws Exception {
        val clientKey = props.getClientKey();
        val clientSecret = props.getClientSecret();
        val config = new Config().setClientKey(clientKey).setClientSecret(clientSecret);

        return new Client(config);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableRetry
    static class CurrentUserProvider {

    }

}

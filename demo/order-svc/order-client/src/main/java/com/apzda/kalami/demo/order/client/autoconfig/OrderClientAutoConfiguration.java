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
package com.apzda.kalami.demo.order.client.autoconfig;

import com.apzda.kalami.demo.order.client.OrderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.context.annotation.Configuration;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class OrderClientAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "feign.slf4j.Slf4jLogger", value = { FallbackFactory.class })
    @EnableFeignClients(clients = { OrderService.class })
    static class OrderClientConfiguration {

    }

}

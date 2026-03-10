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
package com.apzda.hajimi.auditor.autoconfig;

import com.apzda.hajimi.auditor.client.AuditService;
import com.apzda.hajimi.auditor.logging.AuditLoggerFactory;
import com.apzda.hajimi.auditor.logging.AuditLoggerFactoryImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Configuration(proxyBeanMethods = false)
@ComponentScan(value = { "com.apzda.hajimi.auditor" },
        excludeFilters = { @ComponentScan.Filter(classes = { ComponentScan.class }) })
public class AuditorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    AuditLoggerFactory auditLoggerFactory(@Autowired(required = false) AuditService auditService,
            @Autowired(required = false) ObservationRegistry observationRegistry, ObjectMapper objectMapper) {
        return new AuditLoggerFactoryImpl(auditService, objectMapper, observationRegistry);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "feign.slf4j.Slf4jLogger")
    @EnableFeignClients(clients = { AuditService.class, })
    static class AuditorOpenfeignClientConfiguration {

    }

}

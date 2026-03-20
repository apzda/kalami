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
package com.apzda.hajimi.configure;

import com.apzda.hajimi.configure.service.EnableConfigureService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/

@SpringBootApplication
@EnableConfigureService
public class TestApp {

    @TestConfiguration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "skip.container", havingValue = "no", matchIfMissing = true)
    @SuppressWarnings("all")
    static class TestConfig {

        @Bean
        @ServiceConnection(name = "redis")
        GenericContainer<?> redis() {
            return new GenericContainer<>(DockerImageName.parse("redis:7.2.4-alpine3.19")).withExposedPorts(6379)
                .withStartupTimeout(Duration.ofMinutes(3));
        }

        @Bean
        @ServiceConnection
        MySQLContainer<?> mysql() {
            return new MySQLContainer<>(DockerImageName.parse("mysql:8.0.35")).withDatabaseName("demo_db")
                .withUsername("root")
                .withPassword("Abc12332!")
                .withStartupTimeout(Duration.ofMinutes(3));
        }

    }

}

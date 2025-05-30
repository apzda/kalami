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

/*
 * Copyright (C) 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.mq.rocket.test;

import com.apzda.kalami.infra.service.LocalInfraServiceImpl;
import com.apzda.kalami.mq.rocket.config.RocketMqConfigProperties;
import com.apzda.kalami.mq.rocket.listener.IMessageListener;
import com.apzda.kalami.mq.rocket.test.callback.DemoSendCallback;
import com.apzda.kalami.mq.rocket.test.listener.DemoMessageListener;
import com.apzda.kalami.mq.rocket.test.message.SimpleMessage;
import com.apzda.kalami.mq.rocket.test.producer.SimpleMessageProducer;
import com.apzda.kalami.service.CounterService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Clock;
import java.time.Duration;

/**
 * @author fengz (windywany@gmail.com)
 * @version 3.4.0
 * @since 3.4.0
 **/
@SpringBootApplication(proxyBeanMethods = false)
public class TestApp {

    @Testcontainers
    @TestConfiguration(proxyBeanMethods = false)
    @ConditionalOnProperty(name = "skip.container", havingValue = "no", matchIfMissing = true)
    static class TestConfigure {

        @Bean
        @ServiceConnection
        @SuppressWarnings("all")
        MySQLContainer<?> mysql() {
            return new MySQLContainer<>(DockerImageName.parse("mysql:8.0.35")).withDatabaseName("demo_db")
                .withUsername("root")
                .withPassword("Abc12332!")
                .withStartupTimeout(Duration.ofMinutes(3));
        }

    }

    @Bean
    IMessageListener<SimpleMessage, Tags> demoListener() {
        return new DemoMessageListener();
    }

    @Bean
    IMessageListener<SimpleMessage, Tags> delayListener() {
        return new DemoMessageListener();
    }

    @Bean
    @Qualifier("simpleMessageProducer")
    SimpleMessageProducer simpleMessageProducer(RocketMqConfigProperties properties) {
        return new SimpleMessageProducer(properties.getProducers().get("test"));
    }

    @Bean
    @Qualifier("failedMessageProducer")
    SimpleMessageProducer failedMessageProducer(RocketMqConfigProperties properties) {
        return new SimpleMessageProducer(properties.getProducers().get("demox"));
    }

    @Bean
    @Qualifier("directMessageProducer")
    SimpleMessageProducer directMessageProducer(RocketMqConfigProperties properties) {
        return new SimpleMessageProducer(properties.getProducers().get("direct"));
    }

    @Bean
    @Qualifier("transMessageProducer")
    SimpleMessageProducer transMessageProducer(RocketMqConfigProperties properties) {
        return new SimpleMessageProducer(properties.getProducers().get("trans"));
    }

    @Bean
    @Qualifier("delayMessageProducer")
    SimpleMessageProducer delayMessageProducer(RocketMqConfigProperties properties) {
        return new SimpleMessageProducer(properties.getProducers().get("delay"));
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    CounterService counter() {
        return new LocalInfraServiceImpl(Duration.ofMinutes(120));
    }

    @Bean
    DemoSendCallback demoCallback() {
        return new DemoSendCallback();
    }

}

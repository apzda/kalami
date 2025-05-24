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

package com.apzda.kalami.mq.rocket.autoconfig;

import com.apzda.kalami.mq.messenger.IMessenger;
import com.apzda.kalami.mq.rocket.config.RocketMqConfigProperties;
import com.apzda.kalami.mq.rocket.consumer.AbstractConsumer;
import com.apzda.kalami.mq.rocket.consumer.DefaultConsumer;
import com.apzda.kalami.mq.rocket.domain.service.IMailboxService;
import com.apzda.kalami.mq.rocket.domain.service.impl.MailboxServiceImpl;
import com.apzda.kalami.mq.rocket.limiter.DefaultRateLimiter;
import com.apzda.kalami.mq.rocket.limiter.RateLimiter;
import com.apzda.kalami.mq.rocket.listener.ISendCallback;
import com.apzda.kalami.mq.rocket.messenger.SimpleMessengerImpl;
import com.apzda.kalami.mq.rocket.messenger.TransactionalMessengerImpl;
import com.apzda.kalami.mq.rocket.transaction.RocketMqTransactionChecker;
import com.apzda.kalami.service.CounterService;
import com.baomidou.mybatisplus.extension.spring.MybatisPlusApplicationContextAware;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.TransactionChecker;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.css.Counter;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/22
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RocketMqConfigProperties.class)
class RocketConfiguration {

    @Bean
    @ConditionalOnMissingBean
    static TransactionChecker transactionChecker(IMailboxService mailboxService, ObjectMapper objectMapper,
            ObjectProvider<ISendCallback> callbackProvider) {
        return new RocketMqTransactionChecker(mailboxService, objectMapper, callbackProvider.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    static ClientConfiguration clientConfiguration(RocketMqConfigProperties properties) {
        val sessionCredentialsProvider = new StaticSessionCredentialsProvider(properties.getUsername(),
                properties.getPassword());
        return ClientConfiguration.newBuilder()
            .setEndpoints(properties.getEndpoints())
            .setNamespace(properties.getNamespace())
            .setRequestTimeout(properties.getRequestTimeout())
            .enableSsl(properties.isSslEnabled())
            .setCredentialProvider(sessionCredentialsProvider)
            .build();
    }

    @Bean
    @ConditionalOnMissingBean
    static ClientServiceProvider clientServiceProvider() {
        return ClientServiceProvider.loadService();
    }

    @Bean("defaultRocketConsumer")
    @ConditionalOnMissingBean(name = "defaultRocketConsumer")
    @ConditionalOnProperty(name = "kalami.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnExpression("${kalami.rocketmq.consumers.default.enabled:true} && '${kalami.rocketmq.consumers.default.group:}' != ''")
    AbstractConsumer defaultRocketConsumer(ClientServiceProvider clientServiceProvider,
            ClientConfiguration clientConfiguration, RocketMqConfigProperties rocketConfigProperties) {
        val config = rocketConfigProperties.getConsumers().get("default");
        return new DefaultConsumer(clientServiceProvider, clientConfiguration, config);
    }

    @Primary
    @Bean(destroyMethod = "close")
    @Qualifier("SimpleRocketProducer")
    @ConditionalOnMissingBean(name = "producer")
    @ConditionalOnProperty(name = "kalami.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
    static Producer producer(ClientServiceProvider provider, ClientConfiguration configuration) throws ClientException {
        return provider.newProducerBuilder().setClientConfiguration(configuration).build();
    }

    @Bean(destroyMethod = "close")
    @Qualifier("TransactionalProducer")
    @ConditionalOnProperty(name = "kalami.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
    static Producer transactionalProducer(ClientServiceProvider provider, ClientConfiguration configuration,
            RocketMqConfigProperties properties, TransactionChecker transactionChecker) throws ClientException {

        val builder = provider.newProducerBuilder()
            .setClientConfiguration(configuration)
            .setTransactionChecker(transactionChecker)
            .setMaxAttempts(properties.getMaxAttempts());

        val topics = properties.getProducers()
            .values()
            .stream()
            .filter(RocketMqConfigProperties.ProducerConfig::isTransaction)
            .map(RocketMqConfigProperties.ProducerConfig::getTopic)
            .toList();

        if (!CollectionUtils.isEmpty(topics)) {
            builder.setTopics(topics.toArray(new String[0]));
        }
        return builder.build();
    }

    @Bean("simpleMessenger")
    @Qualifier("SimpleMessengerImpl")
    @ConditionalOnMissingBean(name = "simpleMessenger")
    IMessenger messenger(ClientServiceProvider provider, ObjectMapper objectMapper,
            @Autowired(required = false) Producer producer, @Autowired(required = false) RateLimiter limiter,
            ObjectProvider<ISendCallback> sendCallback) {
        return new SimpleMessengerImpl(provider, producer, limiter, objectMapper,
                sendCallback.orderedStream().toList());
    }

    @Bean("transactionalMessenger")
    @Qualifier("TransactionalMessengerImpl")
    @ConditionalOnMissingBean(name = "transactionalMessenger")
    @ConditionalOnClass(Transactional.class)
    IMessenger transactionalMessenger(RocketMqConfigProperties properties, ObjectMapper objectMapper,
            @Autowired(required = false) @Qualifier("TransactionalProducer") Producer producer,
            @Autowired(required = false) @Qualifier("SimpleRocketProducer") Producer simpleProducer,
            IMailboxService mailboxService, ClientServiceProvider clientServiceProvider,
            ObjectProvider<ISendCallback> sendCallback) {
        return new TransactionalMessengerImpl(properties, producer == null ? simpleProducer : producer, objectMapper,
                mailboxService, clientServiceProvider, sendCallback.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(Counter.class)
    @ConditionalOnProperty(name = "kalami.rocketmq.limit.enabled", havingValue = "true")
    RateLimiter defaultRateLimiter(CounterService counter, RocketMqConfigProperties properties,
            ObjectMapper objectMapper, ObjectProvider<ISendCallback> sendCallback) {
        return new DefaultRateLimiter(counter, objectMapper, properties, sendCallback.orderedStream().toList());
    }

    @Configuration
    @ConditionalOnClass(MybatisPlusApplicationContextAware.class)
    @ConditionalOnMissingBean(IMailboxService.class)
    @MapperScan("com.apzda.kalami.mq.rocket.domain.mapper")
    static class DefaultMailBoxServiceConfiguration {

        @Bean
        IMailboxService KalamiRocketMqMailboxService() {
            return new MailboxServiceImpl();
        }

    }

}

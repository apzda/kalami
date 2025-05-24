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

package com.apzda.kalami.mq.rocket.consumer;

import com.apzda.kalami.mq.rocket.config.RocketMqConfigProperties;
import jakarta.annotation.Nonnull;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/22
 * @version 1.0.0
 */
public class DefaultConsumer extends AbstractConsumer {

    public DefaultConsumer(@Nonnull ClientServiceProvider clientServiceProvider,
            @Nonnull ClientConfiguration clientConfiguration, @Nonnull RocketMqConfigProperties.ConsumerConfig config) {
        super(clientServiceProvider, clientConfiguration, config);
    }

}

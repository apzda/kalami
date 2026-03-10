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
package com.apzda.hajimi.configure.config;

import com.apzda.hajimi.configure.event.SettingChangedEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import static com.apzda.hajimi.configure.listener.SettingChangedListener.BEAN_NAME;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(StringRedisTemplate.class)
@Slf4j
class RedisConfiguration {

    @Bean(BEAN_NAME)
    // @ConditionalOnBean(value = { StringRedisTemplate.class })
    @ConditionalOnMissingBean(name = BEAN_NAME)
    ApplicationListener<SettingChangedEvent> configSettingChangedListener(StringRedisTemplate redisTemplate,
            @Qualifier("configChangedMessageTopic") ChannelTopic configChangedMessageTopic) {
        log.debug("Redis Setting Changed Listener configured!");

        return event -> {
            val settingKey = event.getSource().toString();
            log.info("Refresh Setting({})", settingKey);
            redisTemplate.convertAndSend(configChangedMessageTopic.getTopic(), settingKey);
        };
    }

}

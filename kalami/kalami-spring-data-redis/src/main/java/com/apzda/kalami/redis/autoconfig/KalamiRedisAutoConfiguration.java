/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.redis.autoconfig;

import com.apzda.kalami.redis.service.RedisBasedInfraServiceImpl;
import com.apzda.kalami.web.autoconfig.KalamiWebAutoConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(RedisAutoConfiguration.class)
@AutoConfigureBefore(KalamiWebAutoConfiguration.class)
@ConditionalOnClass({ StringRedisTemplate.class })
public class KalamiRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ StringRedisTemplate.class })
    RedisBasedInfraServiceImpl kalamiInfraCounterService(StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper) {
        return new RedisBasedInfraServiceImpl(stringRedisTemplate, objectMapper);
    }

}

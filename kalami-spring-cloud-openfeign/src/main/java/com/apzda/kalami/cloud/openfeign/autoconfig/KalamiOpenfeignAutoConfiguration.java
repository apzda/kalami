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
package com.apzda.kalami.cloud.openfeign.autoconfig;

import com.apzda.kalami.cloud.openfeign.config.KalamiServiceProperties;
import com.apzda.kalami.cloud.openfeign.interceptor.TracingFeignRequestInterceptor;
import com.apzda.kalami.cloud.openfeign.transfomer.FeignExceptionTransformer;
import com.apzda.kalami.http.ExceptionTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@EnableConfigurationProperties(KalamiServiceProperties.class)
@Configuration(proxyBeanMethods = false)
public class KalamiOpenfeignAutoConfiguration {

    @Bean
    TracingFeignRequestInterceptor tracingFeignRequestInterceptor(
            @Value("${kalami.security.feign-enabled:false}") boolean feignEnabled) {
        return new TracingFeignRequestInterceptor(feignEnabled);
    }

    @Bean
    ExceptionTransformer feignExceptionTransformer() {
        return new FeignExceptionTransformer();
    }

}

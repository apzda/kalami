/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.web.autoconfig;

import com.apzda.kalami.web.tracing.TracingRestTemplateInterceptor;
import com.apzda.kalami.web.tracing.TracingRestTemplateInterceptorAfterPropertiesSet;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
class TracingRestClientConfiguration {

    @Bean
    @ConditionalOnBean(ObservationRegistry.class)
    static TracingRestTemplateInterceptor tracingRestTemplateInterceptor() {
        return new TracingRestTemplateInterceptor();
    }

    @Bean
    @ConditionalOnBean(ObservationRegistry.class)
    TracingRestTemplateInterceptorAfterPropertiesSet tracingRestTemplateInterceptorAfterPropertiesSet(
            TracingRestTemplateInterceptor tracingRestTemplateInterceptor,
            ObjectProvider<RestTemplate> objectProvider) {
        return new TracingRestTemplateInterceptorAfterPropertiesSet(tracingRestTemplateInterceptor, objectProvider);
    }

}

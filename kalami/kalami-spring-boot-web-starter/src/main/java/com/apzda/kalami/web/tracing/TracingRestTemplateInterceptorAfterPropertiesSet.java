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
package com.apzda.kalami.web.tracing;

import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
public class TracingRestTemplateInterceptorAfterPropertiesSet implements InitializingBean {

    private final Collection<RestTemplate> restTemplates;

    private final TracingRestTemplateInterceptor tracingRestTemplateInterceptor;

    public TracingRestTemplateInterceptorAfterPropertiesSet(
            TracingRestTemplateInterceptor tracingRestTemplateInterceptor,
            ObjectProvider<RestTemplate> restTemplateProvider) {
        this.tracingRestTemplateInterceptor = tracingRestTemplateInterceptor;
        restTemplates = restTemplateProvider.orderedStream().toList();
    }

    public void afterPropertiesSet() {
        if (!CollectionUtils.isEmpty(restTemplates)) {
            for (val restTemplate : this.restTemplates) {
                final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
                interceptors.add(this.tracingRestTemplateInterceptor);
                restTemplate.setInterceptors(interceptors);
            }
        }

    }

}

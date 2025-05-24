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
package com.apzda.kalami.cloud.openfeign.interceptor;

import com.apzda.kalami.web.context.KalamiContextHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.val;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public class TracingFeignRequestInterceptor implements RequestInterceptor {

    private final boolean feignEnabled;

    public TracingFeignRequestInterceptor(boolean feignEnabled) {
        this.feignEnabled = feignEnabled;
    }

    @Override
    public void apply(RequestTemplate template) {
        String rid = KalamiContextHolder.getRequestId();

        if (StringUtils.hasLength(rid)) {
            template.header("X-Request-ID", rid);
        }
        val headers = KalamiContextHolder.headers();
        val userAgent = headers.get("User-Agent");
        if (!CollectionUtils.isEmpty(userAgent)) {
            template.header("User-Agent", userAgent);
        }

        if (feignEnabled) {
            val authorization = headers.get("Authorization");
            if (!CollectionUtils.isEmpty(authorization)) {
                template.header("Authorization", authorization);
            }

            val app = headers.get("X-App");
            if (!CollectionUtils.isEmpty(app)) {
                template.header("X-App", app);
            }
            val device = headers.get("X-Device");
            if (!CollectionUtils.isEmpty(device)) {
                template.header("X-Device", device);
            }

            val metaHeaders = KalamiContextHolder.headers("X-M-");
            if (!CollectionUtils.isEmpty(metaHeaders)) {
                metaHeaders.forEach(template::header);
            }
        }

    }

}

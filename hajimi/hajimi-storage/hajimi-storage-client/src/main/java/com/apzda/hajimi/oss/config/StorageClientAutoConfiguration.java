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
package com.apzda.hajimi.oss.config;

import com.apzda.hajimi.oss.backend.OssBackend;
import com.apzda.hajimi.oss.client.StorageService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.data.util.Lazy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
//@formatter:off
@Import({
    AliOssBackendConfiguration.class,
    MinioBackendConfiguration.class,
    TxCosBackendConfiguration.class,
    HwObsBackendConfiguration.class,
    FileBackendConfiguration.class
})
//@formatter:on
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(StorageConfigProperties.class)
@Slf4j
public class StorageClientAutoConfiguration {

    private static final BackendConfig DEFAULT_BACKEND_CONFIG = new BackendConfig();

    private final StorageConfigProperties properties;

    private final ApplicationContext context;

    private final Lazy<OssBackend> backend;

    private final Map<String, OssBackend> ossBackends;

    public StorageClientAutoConfiguration(StorageConfigProperties properties, ApplicationContext context) {
        this.properties = properties;
        this.context = context;
        this.ossBackends = new ConcurrentHashMap<>();

        this.backend = Lazy.of(() -> {
            val backend = StringUtils.defaultIfBlank(properties.getBackend(), "fs");
            return getBackend(backend);
        });

        new StorageManager(context) {
        };
    }

    String theRealBackend(String backend) {
        return StringUtils.defaultIfBlank(backend, StringUtils.defaultIfBlank(properties.getBackend(), "fs"));
    }

    OssBackend getBackend() {
        return this.backend.get();
    }

    OssBackend getBackend(String backendId) {
        val backend = StringUtils.defaultIfBlank(backendId, StringUtils.defaultIfBlank(properties.getBackend(), "fs"));
        if (this.ossBackends.containsKey(backend)) {
            return this.ossBackends.get(backend);
        }

        synchronized (this.ossBackends) {
            if (this.ossBackends.containsKey(backend)) {
                return this.ossBackends.get(backend);
            }
            OssBackend ossBackend;

            val backends = properties.getBackends();
            val config = backends.getOrDefault(backend, DEFAULT_BACKEND_CONFIG);

            val clazz = config.getClazz();
            if (clazz == null) {
                try {
                    ossBackend = context.getBean(backend + "OssBackend", OssBackend.class);
                    log.debug("Found oss backend by name: {}OssBackend - {}", backend, ossBackend);
                }
                catch (Exception e) {
                    throw new NullPointerException("Clazz of '" + backend + "' is null");
                }
            }
            else {
                ossBackend = BeanUtils.instantiateClass(clazz);
            }
            config.setTmpDir(properties.getTmpDir());

            if (StringUtils.isBlank(config.getPathPatten())) {
                config.setPathPatten(properties.getPathPatten());
            }
            val baseUrl = config.getBaseUrl();
            if (StringUtils.isBlank(baseUrl)) {
                config.setBaseUrl(properties.getBaseUrl());
            }

            if (!ossBackend.init(config)) {
                log.warn("Cannot initialize '{}' OssBackend: {}", backend, config);
            }

            ossBackends.put(backend, ossBackend);
            return ossBackend;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "feign.slf4j.Slf4jLogger")
    @EnableFeignClients(clients = { StorageService.class, })
    static class StorageOpenfeignClientConfiguration {

    }

}

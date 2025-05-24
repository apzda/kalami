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

import com.apzda.kalami.web.config.KalamiServerProperties;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@Slf4j
@Service
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(io.undertow.websockets.jsr.Bootstrap.class)
class KalamiUndertowConfig {

    @Bean
    @ConditionalOnMissingBean(name = "websocketServletWebServerCustomizer")
    public WebServerFactoryCustomizer<UndertowServletWebServerFactory> websocketServletWebServerCustomizer(
            KalamiServerProperties kalamiServerProperties) {
        val undertow = kalamiServerProperties.getUndertow();
        return factory -> {
            factory.addDeploymentInfoCustomizers(deploymentInfo -> {
                WebSocketDeploymentInfo info = new WebSocketDeploymentInfo();

                DefaultByteBufferPool bufferPool = new DefaultByteBufferPool(undertow.isDirect(),
                        (int) undertow.getBufferSize().toBytes(), undertow.getMaximumPoolSize(),
                        undertow.getThreadLocalCacheSize(), undertow.getLeakDetectionPercent());

                log.info("Undertow websocket deployed with: {}", undertow);

                info.setBuffers(bufferPool);

                deploymentInfo.addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, info);
            });
        };
    }

}

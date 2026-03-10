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

import com.apzda.hajimi.oss.cache.FileInfoCache;
import com.apzda.hajimi.oss.cache.LocalFileInfoCache;
import com.apzda.hajimi.oss.controller.function.DownloadHandlerFunction;
import com.apzda.hajimi.oss.controller.function.PreviewHandlerFunction;
import com.apzda.hajimi.oss.plugin.Plugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static com.apzda.hajimi.oss.config.StorageConfigProperties.DOWNLOAD_BEAN_NAME;
import static com.apzda.hajimi.oss.config.StorageConfigProperties.PREVIEW_BEAN_NAME;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ StorageServerConfigProperties.class, StorageConfigProperties.class })
@Import({ ResizePluginConfiguration.class, WatermarkPluginConfiguration.class })
@RequiredArgsConstructor
@ComponentScan("com.apzda.hajimi.oss.controller")
public class StorageServiceConfiguration implements InitializingBean {

    private final ApplicationContext applicationContext;

    private final StorageServerConfigProperties properties;

    private final StorageConfigProperties ossConfigProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        val plugins = properties.getPlugins();
        for (StorageServerConfigProperties.PluginConfig plugin : plugins) {
            val clazz = plugin.getPluginClass();
            if (clazz != null) {
                plugin.instance(BeanUtils.instantiateClass(clazz));
            }
            else if (StringUtils.isNotBlank(plugin.getId())) {
                val pluginBeanName = plugin.getId() + "OssPlugin";
                if (applicationContext.containsBean(pluginBeanName)) {
                    plugin.instance(applicationContext.getBean(pluginBeanName, Plugin.class));
                }
                else {
                    log.warn("Oss Plugin Bean: '{}' is not found!", pluginBeanName);
                    continue;
                }
            }
            else {
                throw new IllegalStateException(
                        "Invalid Plugin Configuration: id or pluginClass at lease one must not be specified!");
            }
            log.debug("Oss Plugin installed: {}", plugin);
            plugin.getProps().put(Plugin.PROP_TMPDIR_S, ossConfigProperties.getTmpDir());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    FileInfoCache fileInfoCache() {
        return new LocalFileInfoCache();
    }

    @Bean(PREVIEW_BEAN_NAME)
    @ConditionalOnProperty(prefix = "kalami.storage.server", name = "preview-path")
    @ConditionalOnMissingBean(name = PREVIEW_BEAN_NAME)
    RouterFunction<ServerResponse> previewOssImageFunc(
            @Value("${kalami.storage.server.preview-path}") String previewPath) {
        val path = "/" + StringUtils.strip(previewPath, "/") + "/";
        log.info("Setup Oss file preview path: {}", previewPath);
        val ant = new AntPathMatcher();
        val pathPattern = path + "**";
        return RouterFunctions.route()
            .GET((request) -> ant.match(pathPattern, request.path()), new PreviewHandlerFunction(path))
            .build();
    }

    @Bean(DOWNLOAD_BEAN_NAME)
    @ConditionalOnProperty(prefix = "kalami.storage.server", name = "download-path")
    @ConditionalOnMissingBean(name = DOWNLOAD_BEAN_NAME)
    RouterFunction<ServerResponse> downloadOssFileFunc(
            @Value("${kalami.storage.server.download-path}") String downloadPath) {
        val path = "/" + StringUtils.strip(downloadPath, "/") + "/";
        log.info("Setup Oss file download path: {}", downloadPath);
        val ant = new AntPathMatcher();
        val pathPattern = path + "**";

        return RouterFunctions.route()
            .GET((request) -> ant.match(pathPattern, request.path()), new DownloadHandlerFunction(path))
            .build();
    }

}

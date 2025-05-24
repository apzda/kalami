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
package com.apzda.kalami.cloud.openfeign.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "kalami.cloud.feign")
public class KalamiServiceProperties {

    private final Map<String, Service> service = new LinkedHashMap<>();

    public Map<String, Service> getService() {
        return service;
    }

    public static class Service {

        private String name;

        private String url;

        private Class<?>[] configuration = new Class<?>[0];

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Class<?>[] getConfiguration() {
            return configuration;
        }

        public void setConfiguration(Class<?>[] configuration) {
            this.configuration = configuration;
        }

    }

}

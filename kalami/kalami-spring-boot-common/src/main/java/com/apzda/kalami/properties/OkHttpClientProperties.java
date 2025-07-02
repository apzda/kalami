/*
 * Copyright 2025 the original author or authors.
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
package com.apzda.kalami.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author john <luxi520cn@163.com>
 */
@Data
@ConfigurationProperties("okhttp")
public class OkHttpClientProperties {
    /**
     * 连接超时(毫秒)
     */
    private int connectTimeOut = 5000;
    /**
     * 读超时(毫秒)
     */
    private int readTimeOut = 30000;
    /**
     * 写超时(毫秒)
     */
    private int writeTimeOut = 30000;
    /**
     * 最大空闲连接数
     */
    private int maxIdleConnections = 10;
    /**
     * 活跃保持时间(毫秒)
     */
    private long keepAliveDuration = 5000;

}

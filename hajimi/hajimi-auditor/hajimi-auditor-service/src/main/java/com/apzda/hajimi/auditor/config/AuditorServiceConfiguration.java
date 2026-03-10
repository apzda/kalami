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
package com.apzda.hajimi.auditor.config;

import com.apzda.kalami.io.YamlPropertySourceFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.apzda.hajimi.auditor.domain.mapper")
//@formatter:off
@ComponentScan({
    "com.apzda.hajimi.auditor.service",
    "com.apzda.hajimi.auditor.controller",
    "com.apzda.hajimi.auditor.domain"
})
//@formatter:on
@PropertySources({ @PropertySource(name = "hji-audit-security-config",
        value = "classpath*:hji-audit-security-config.yml", factory = YamlPropertySourceFactory.class) })
public class AuditorServiceConfiguration {

}

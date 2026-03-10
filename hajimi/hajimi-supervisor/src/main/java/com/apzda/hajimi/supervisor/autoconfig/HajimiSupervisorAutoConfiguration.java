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
package com.apzda.hajimi.supervisor.autoconfig;

import com.apzda.kalami.mybatisplus.autoconfig.KalamiMyBatisPlusAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(KalamiMyBatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(SupervisorConfigProperties.class)
public class HajimiSupervisorAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @MapperScan("com.apzda.hajimi.supervisor.domain.mapper")
    @ComponentScan({ "com.apzda.hajimi.supervisor.domain.service", "com.apzda.hajimi.supervisor.runner",
            "com.apzda.hajimi.supervisor.controller" })
    static class HajimiSupervisorConfiguration {

    }

}

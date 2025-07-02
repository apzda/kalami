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
package com.apzda.kalami.demo.user.server;

import com.apzda.kalami.demo.store.feign.StoreService;
import com.apzda.kalami.demo.user.EnableUserService;
import com.apzda.kalami.service.CounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author fengz (windywany@gmail.com)
 * @since 2025/05/14
 * @version 1.0.0
 */

@SpringBootApplication
@EnableUserService
@EnableFeignClients(basePackageClasses = StoreService.class)
public class UserWebServer implements InitializingBean {

    static Logger log = LoggerFactory.getLogger(UserWebServer.class);

    @Autowired
    private CounterService counterService;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("UserWebServer initialized: {}", counterService.getClass().getName());
    }

    public static void main(String[] args) {
        SpringApplication.run(UserWebServer.class, args);
    }

}

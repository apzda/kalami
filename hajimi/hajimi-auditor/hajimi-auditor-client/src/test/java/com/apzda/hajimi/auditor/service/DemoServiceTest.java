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
package com.apzda.hajimi.auditor.service;

import com.apzda.hajimi.auditor.AuditApp;
import com.apzda.hajimi.auditor.autoconfig.AuditorAutoConfiguration;
import com.apzda.hajimi.auditor.client.AuditService;
import com.apzda.kalami.data.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@SpringBootTest
@ContextConfiguration(classes = AuditApp.class)
@ImportAutoConfiguration({ AuditorAutoConfiguration.class, AopAutoConfiguration.class, SecurityAutoConfiguration.class,
        ObservationAutoConfiguration.class })
@ComponentScan(basePackages = { "com.apzda.hajimi.auditor" })
@TestPropertySource(properties = { "logging.level.com.apzda.hajimi=trace" })
class DemoServiceTest {

    @MockitoBean
    private AuditService auditService;

    @Autowired
    private DemoService demoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publishEventTest() throws InterruptedException {
        // given
        when(auditService.log(any())).thenReturn(Response.ok());
        // when
        demoService.publishEvent();
        TimeUnit.SECONDS.sleep(1);
        // then
        verify(auditService, times(1)).log(any());
    }

}

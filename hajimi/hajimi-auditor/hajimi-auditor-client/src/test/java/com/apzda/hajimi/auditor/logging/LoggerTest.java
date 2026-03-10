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
package com.apzda.hajimi.auditor.logging;

import com.apzda.hajimi.auditor.AuditApp;
import com.apzda.hajimi.auditor.TestVo;
import com.apzda.hajimi.auditor.autoconfig.AuditorAutoConfiguration;
import com.apzda.hajimi.auditor.client.AuditService;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.domain.AuditLog;
import lombok.val;
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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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
@TestPropertySource(properties = { "logging.level.com.apzda.hajimi.auditor=trace" })
class LoggerTest {

    @MockitoBean
    private AuditService auditService;

    @Autowired
    private AuditLoggerFactory loggerFactory;

    @Test
    void log() throws InterruptedException {
        // given
        val map = new HashMap<String, String>();
        given(auditService.log(any())).willAnswer((invocation) -> {
            val argument = invocation.getArgument(0, AuditLog.class);
            map.put("message", argument.getMessage());
            return Response.success(true);
        });

        // when
        loggerFactory.create("test").message("hello world").log();
        TimeUnit.MILLISECONDS.sleep(500);
        // then
        assertThat(map).isNotEmpty();
        assertThat(map).containsKeys("message");
        assertThat(map.get("message")).isEqualTo("hello world");
    }

    @Test
    void sanitize_should_work() throws InterruptedException {
        // given
        val map = new HashMap<String, Object>();
        val tv = new TestVo();
        tv.setPhone("13088888888");
        given(auditService.log(any())).willAnswer((invocation) -> {
            val argument = invocation.getArgument(0, AuditLog.class);
            map.put("message", argument.getMessage());
            map.put("nv", argument.getNewValue());
            return Response.success(true);
        });

        // when
        loggerFactory.create("test").message("hello world").newValue(tv).log();
        TimeUnit.MILLISECONDS.sleep(500);
        // then
        assertThat(map).isNotEmpty();
        assertThat(map).containsKeys("message");
        assertThat(map.get("message")).isEqualTo("hello world");
        assertThat(((TestVo) map.get("nv")).getPhone()).isEqualTo("130****8888");
    }

}

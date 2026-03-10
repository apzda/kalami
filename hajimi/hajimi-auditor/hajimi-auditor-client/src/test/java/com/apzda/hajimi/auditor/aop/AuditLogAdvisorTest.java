/*
 * Copyright 2023-2026 the original author or authors.
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
package com.apzda.hajimi.auditor.aop;

import com.apzda.hajimi.auditor.AuditApp;
import com.apzda.hajimi.auditor.TestVo;
import com.apzda.hajimi.auditor.autoconfig.AuditorAutoConfiguration;
import com.apzda.hajimi.auditor.client.AuditService;
import com.apzda.hajimi.auditor.controller.DemoController;
import com.apzda.hajimi.auditor.service.DemoService;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.domain.AuditLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = AuditApp.class)
@ImportAutoConfiguration(
        classes = { AuditorAutoConfiguration.class, AopAutoConfiguration.class, SecurityAutoConfiguration.class })
@ComponentScan(basePackages = { "com.apzda.hajimi.auditor" })
@TestPropertySource(properties = { "logging.level.com.apzda.hajimi=trace" })
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
public class AuditLogAdvisorTest {

    @Autowired
    private DemoController demoController;

    @Autowired
    private DemoService demoService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuditService auditService;

    @Test
    void advisor_should_be_work_ok() throws InterruptedException {
        // given
        val map = new ArrayList<String>();
        given(auditService.log(any())).willAnswer((invocation) -> {
            val argument = invocation.getArgument(0, AuditLog.class);
            map.add(argument.getMessage());
            map.add(argument.getRunas());
            map.add(argument.getDevice());
            return Response.success(true);
        });
        // when
        val str = demoController.shouldBeAudited("123");
        // then
        assertThat(str).isEqualTo("hello ya:123");
        // when
        TimeUnit.MILLISECONDS.sleep(500);
        // then
        assertThat(map).isNotEmpty();
        assertThat(map.get(0)).isEqualTo("you are get then id is: 123, then result is:hello ya:123");
        assertThat(map.get(1)).isEqualTo("123");
        assertThat(map.get(2)).isEqualTo("test");
    }

    @Test
    void advisor_should_be_work_ok2() throws InterruptedException {
        // given
        val map = new HashMap<Integer, String>();
        given(auditService.log(any())).willAnswer((invocation) -> {
            val argument = invocation.getArgument(0, AuditLog.class);
            map.put(0, argument.getArgs().get(0));
            return Response.success(true);
        });
        // when
        val str = demoService.hello();
        // then
        assertThat(str.getErrMsg()).isEqualTo("error message");
        // when
        TimeUnit.MILLISECONDS.sleep(500);
        // then
        assertThat(map).isNotEmpty();
        assertThat(map).containsKeys(0);
        assertThat(map.get(0)).isEqualTo("error message");
    }

    @Test
    void advisor_should_be_work_ok3() throws InterruptedException {
        // given
        val map = new HashMap<Integer, String>();
        given(auditService.log(any())).willAnswer((invocation) -> {
            val argument = invocation.getArgument(0, AuditLog.class);
            map.put(0, argument.getArgs().get(0));
            map.put(1, argument.getArgs().get(1));
            map.put(3, String.valueOf(argument.getTemplate()));
            return Response.success(true);
        });
        // when
        val str = demoService.hello("hi");
        // then
        assertThat(str.getErrMsg()).isEqualTo("error hi");
        // when
        TimeUnit.MILLISECONDS.sleep(500);
        // then
        assertThat(map).isNotEmpty();
        assertThat(map).containsKeys(0, 1);
        assertThat(map.get(1)).isEqualTo("hi");
        assertThat(map.get(0)).isEqualTo("error hi");
        assertThat(map.get(3)).isEqualTo("true");
    }

    @Test
    void advisor_should_be_work_ok4() throws InterruptedException {
        // given
        val map = new HashMap<Integer, String>();
        given(auditService.log(any())).willAnswer((invocation) -> {
            val argument = invocation.getArgument(0, AuditLog.class);
            map.put(0, argument.getMessage());
            return Response.success(true);
        });
        // when
        assertThatThrownBy(() -> {
            demoService.hello2("hi");
        }).hasMessage("hi is invalid");
        // when
        TimeUnit.MILLISECONDS.sleep(500);
        // then
        assertThat(map).isNotEmpty();
        assertThat(map.get(0)).isEqualTo("Arg \"hi\":hi is invalid");
    }

    @Test
    void advisor_should_be_work_ok5() throws InterruptedException {
        // given
        val map = new HashMap<Integer, String>();
        given(auditService.log(any())).willAnswer((invocation) -> {
            val argument = invocation.getArgument(0, AuditLog.class);
            map.put(0, argument.getArgs().get(0));
            map.put(1, argument.getArgs().get(1));
            map.put(3, argument.getMessage());
            map.put(4, String.valueOf(argument.getTemplate()));
            map.put(5, argument.getLevel());
            return Response.success(true);
        });
        // when
        assertThatThrownBy(() -> {
            demoService.hello3("hi");
        }).hasMessage("hi is invalid");
        TimeUnit.MILLISECONDS.sleep(500);

        // then
        assertThat(map).isNotEmpty();
        assertThat(map).containsKeys(0, 1);
        assertThat(map.get(0)).isEqualTo("hi is invalid");
        assertThat(map.get(1)).isEqualTo("hi");
        assertThat(map.get(3)).isEqualTo("exception message is {}, arg is {}");
        assertThat(map.get(4)).isEqualTo("true");
        assertThat(map.get(5)).isEqualTo("error");
    }

    @Test
    void advisor_should_be_work_ok6(@Autowired WebTestClient webClient) throws Exception {
        webClient.get()
            .uri("/audit/1")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(String.class)
            .isEqualTo("hello ya:1");
    }

    @Test
    void advisor_should_be_work_ok7() throws InterruptedException {
        // given
        val map = new ArrayList<String>();
        given(auditService.log(any())).willAnswer((invocation) -> {
            val argument = invocation.getArgument(0, AuditLog.class);
            map.add(argument.getMessage());
            return Response.success(true);
        });
        // when
        val str = demoController.shouldBeAudited1("123");
        // then
        assertThat(str).isEqualTo("hello ya:123");
        // when
        TimeUnit.MILLISECONDS.sleep(500);
        // then
        assertThat(map).isNotEmpty();
        assertThat(map.size()).isEqualTo(1);
        assertThat(map.get(0)).isEqualTo("new = 1");
    }

    @Test
    void advisor_should_be_work_ok8() throws InterruptedException {
        // given
        val map = new ArrayList<Object>();
        given(auditService.log(any())).willAnswer((invocation) -> {
            val argument = invocation.getArgument(0, AuditLog.class);
            map.add(argument.getNewValue());
            return Response.success(true);
        });
        // when
        val str = demoController.shouldBeAudited3("123");
        // then
        assertThat(str).isEqualTo("hello ya:123");
        // when
        TimeUnit.MILLISECONDS.sleep(500);
        // then
        assertThat(map).isNotEmpty();
        assertThat(((TestVo) map.get(0)).getPhone()).isEqualTo("130****8888");
    }

}

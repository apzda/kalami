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
package com.apzda.hajimi.auditor.controller.internal;

import com.apzda.hajimi.auditor.client.AuditService;
import com.apzda.hajimi.auditor.domain.repository.AuditLogRepository;
import com.apzda.hajimi.auditor.test.TestApp;
import com.apzda.kalami.data.domain.AuditLog;
import com.apzda.kalami.web.autoconfig.KalamiWebAutoConfiguration;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@MybatisPlusTest
@ContextConfiguration(classes = TestApp.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureDataRedis
@AutoConfigureJson
@ImportAutoConfiguration(KalamiWebAutoConfiguration.class)
@Testcontainers(parallel = true)
@Sql(value = { "file:../../schema/mysql/1.0.0.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class AuditServiceControllerTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    AuditService auditService;

    @Autowired
    AuditLogRepository logRepository;

    @Test
    void log() {
        // given
        val auditLog = new AuditLog();
        auditLog.setTimestamp(System.currentTimeMillis());
        auditLog.setUserId("1");
        auditLog.setActivity("test");
        auditLog.setTenantId("0");
        auditLog.setTemplate(false);
        auditLog.setTarget("t");
        auditLog.setIp("127.0.0.1");
        auditLog.setDevice("pc");
        auditLog.setMessage("hei hei");
        auditLog.setArgs(List.of("a1", "a2", "a3"));
        auditLog.setOldValue("abc");
        auditLog.setNewValue("def");

        // when
        val log = auditService.log(auditLog);
        // then
        assertThat(log).isNotNull();
        assertThat(log.getData()).isEqualTo(true);

        // when
        val logs = logRepository.list();
        // then
        assertThat(logs.size()).isEqualTo(1);
    }

}

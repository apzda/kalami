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
package com.apzda.hajimi.sms.controller.internal;

import com.apzda.hajimi.sms.client.SmsService;
import com.apzda.hajimi.sms.client.dto.SendSmsDTO;
import com.apzda.hajimi.sms.core.dto.Variable;
import com.apzda.hajimi.sms.test.TestApp;
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
class SmsServiceFeignControllerTest {

    @Autowired
    SmsService smsService;

    @Test
    void send() {
        // given
        val dto = new SendSmsDTO();
        dto.setTid("test");
        dto.setPhones(List.of("18049920019"));
        dto.setVariables(List.of(new Variable("code", "123456", 0)));
        dto.setSync(false);

        val res = smsService.send(dto);

        assertThat(res.getErrCode()).isEqualTo(0);
        assertThat(res.getData()).isEqualTo(180);
    }

}

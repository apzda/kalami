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
package com.apzda.hajimi.configure.controller.internal;

import com.apzda.hajimi.configure.TestApp;
import com.apzda.hajimi.configure.TestSetting;
import com.apzda.hajimi.configure.autoconfig.ConfigureAutoConfiguration;
import com.apzda.hajimi.configure.client.ConfigureService;
import com.apzda.hajimi.configure.client.dto.RestoreReq;
import com.apzda.hajimi.configure.client.dto.RevisionReq;
import com.apzda.hajimi.configure.client.dto.SaveReq;
import com.apzda.hajimi.configure.exception.SettingUnavailableException;
import com.apzda.hajimi.configure.service.SettingService;
import com.apzda.kalami.mybatisplus.autoconfig.KalamiMyBatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@MybatisPlusTest
@ContextConfiguration(classes = TestApp.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureJson
@AutoConfigureDataRedis
@ImportAutoConfiguration({ ConfigureAutoConfiguration.class, KalamiMyBatisPlusAutoConfiguration.class })
@ActiveProfiles({ "test" })
@Testcontainers(parallel = true)
@Sql(value = { "file:../../schema/mysql/1.0.0.sql", "classpath:test_data.sql" },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "classpath:reset.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ConfigureServiceControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConfigureService configService;

    @Autowired
    private SettingService settingService;

    @Test
    void load_should_be_ok() {
        // when
        val res = configService.load(TestSetting.class.getCanonicalName());

        // then
        assertThat(res).isNotNull();
        assertThat(res.getErrCode()).isEqualTo(404);
    }

    @Test
    void save_restore_should_be_ok() throws SettingUnavailableException, InterruptedException, JsonProcessingException {
        // given
        val settingCls = SettingService.getMeta(TestSetting.class).getSettingCls();
        val ts = new TestSetting();
        ts.setAge(18);
        ts.setName("gsvc");
        ts.setAddress(List.of("a1", "d1", "c2"));

        val saveReq = new SaveReq();
        saveReq.setClazz(settingCls);
        saveReq.setSetting(objectMapper.writeValueAsString(ts));

        // when
        val res = configService.save(saveReq);
        // revision = 1
        // then
        assertThat(res).isNotNull();
        assertThat(res.getErrCode()).isEqualTo(0);

        // when
        val res1 = configService.load(settingCls);

        // then
        assertThat(res1).isNotNull();
        assertThat(res1.getErrCode()).isEqualTo(0);
        assertThat(res1.getData()).isNotNull();

        // when
        var setting = settingService.load(TestSetting.class);
        assertThat(setting).isNotNull();
        assertThat(setting.getAge()).isEqualTo(18);
        assertThat(setting.getName()).isEqualTo("gsvc");
        assertThat(setting.getAddress()).contains("a1", "d1", "c2");

        // given
        ts.setAge(20);
        ts.setAddress(List.of("a1", "d1", "c2", "e3"));
        val saveReq2 = new SaveReq();
        saveReq2.setClazz(settingCls);
        saveReq2.setSetting(objectMapper.writeValueAsString(ts));
        // when
        val res2 = configService.save(saveReq2);
        // revision = 2
        // then
        assertThat(res2).isNotNull();
        assertThat(res2.getErrCode()).isEqualTo(0);

        TimeUnit.SECONDS.sleep(3);
        // when
        setting = settingService.load(TestSetting.class);
        // then
        assertThat(setting).isNotNull();
        assertThat(setting.getAge()).isEqualTo(20);
        assertThat(setting.getName()).isEqualTo("gsvc");
        assertThat(setting.getAddress()).contains("a1", "d1", "c2", "e3");

        // given
        val b3 = new RevisionReq();
        b3.setSettingCls(settingCls);
        // when
        val revisions = configService.revisions(b3);
        // then

        val revision = revisions.getData().get(0);
        System.out.println("revisions = " + revisions.getData());
        assertThat(revision.getRevision()).isEqualTo(1);
        assertThat(revision.getCreatedAt()).isGreaterThan(0);

        // restore
        // given
        val restoreReq = new RestoreReq().setClazz(settingCls).setRevision(revision.getRevision());
        // when - restore
        val restoreRes = configService.restore(restoreReq);
        // then
        assertThat(restoreRes.getErrCode()).isEqualTo(0);
        TimeUnit.SECONDS.sleep(3);
        // when
        setting = settingService.load(TestSetting.class);
        // then
        assertThat(setting).isNotNull();
        assertThat(setting.getAge()).isEqualTo(18);
        assertThat(setting.getName()).isEqualTo("gsvc");
        assertThat(setting.getAddress()).contains("a1", "d1", "c2");

        // when - get revisions again
        val revisions4 = configService.revisions(b3);
        // then

        assertThat(revisions4.getData().size()).isEqualTo(2);
        val revision4 = revisions4.getData().get(0);
        assertThat(revision4.getRevision()).isEqualTo(2);
        assertThat(revision4.getCreatedAt()).isGreaterThan(0);

    }

}

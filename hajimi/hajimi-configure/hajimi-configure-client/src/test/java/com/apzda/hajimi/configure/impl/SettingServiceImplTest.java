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
package com.apzda.hajimi.configure.impl;

import com.apzda.hajimi.configure.TestApplication;
import com.apzda.hajimi.configure.autoconfig.ConfigureAutoConfiguration;
import com.apzda.hajimi.configure.client.ConfigureService;
import com.apzda.hajimi.configure.exception.SettingUnavailableException;
import com.apzda.hajimi.configure.service.SettingService;
import com.apzda.hajimi.configure.setting.TestSetting;
import com.apzda.kalami.data.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@JsonTest
@ContextConfiguration(classes = TestApplication.class)
@ImportAutoConfiguration({ RedisAutoConfiguration.class, ConfigureAutoConfiguration.class })
@TestPropertySource(properties = { "logging.level.com.apzda=debug" })
@ActiveProfiles("test")
@Testcontainers
class SettingServiceImplTest {

    @MockitoBean
    private ConfigureService configService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SettingService settingService;

    @Test
    void loadSetting() throws SettingUnavailableException, JsonProcessingException {
        // given
        val testSetting = new TestSetting();
        testSetting.setName("gsvc");
        testSetting.setAge(18);

        final Response<String> builder = Response.success(objectMapper.writeValueAsString(testSetting));

        given(configService.load(any(String.class))).willAnswer(invocation -> builder);

        // when
        val setting = settingService.load(TestSetting.class);

        // then
        assertThat(setting.getAge()).isEqualTo(18);
        assertThat(setting.getName()).isEqualTo("gsvc");
    }

}

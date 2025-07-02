/*
 * Copyright 2025 the original author or authors.
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
package com.apzda.kalami.boot.aop;

import com.apzda.kalami.boot.TestApp;
import com.apzda.kalami.boot.autoconfig.KalamiXkaAutoConfiguration;
import com.apzda.kalami.boot.controller.TestController;
import com.apzda.kalami.boot.dict.TransformUtils;
import com.apzda.kalami.boot.mapper.DictItemMapper;
import com.apzda.kalami.boot.transformer.Upper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@SpringBootTest
@ContextConfiguration(classes = TestApp.class)
@ImportAutoConfiguration(classes = { AopAutoConfiguration.class, KalamiXkaAutoConfiguration.class })
@TestPropertySource(properties = { "logging.level.com.apzda.kalami=trace" })
@AutoConfigureMockMvc
@Sql(value = "classpath:/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class DictionaryAdvisorTest {

    @MockitoSpyBean
    private DictItemMapper dictItemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestController testController;

    @Test
    void testResponse() {
        val testVoResponse = testController.testVoResponse("1");
        val node = objectMapper.convertValue(testVoResponse, JsonNode.class);
        val result = node.get("data");
        assertThat(result.get("name").asText()).isEqualTo("test = 1");
        assertThat(result.get("statusText").asText()).isEqualTo("status1");
        assertThat(result.get("status2Text").asText()).isEqualTo("status2");
        assertThat(result.get("status3Text").asText()).isEqualTo("T3");
        assertThat(result.get("phone").asText()).isEqualTo("131****6666");
        assertThat(result.get("phone1").asText()).isEqualTo("131****6666");
        assertThat(result.get("phone2").asText()).isEqualTo("13166666666");
        assertThat(result.get("phone2Text").asText()).isEqualTo("13166666666-test");
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGetUserList() {
        // given
        val upper = spy(Upper.class);
        try (val mocked = Mockito.mockStatic(TransformUtils.class)) {
            mocked.when(() -> {
                TransformUtils.getTransformer(Upper.class);
            }).thenReturn(upper);

            // when
            val users = testController.getUserList();
            // then
            val data = objectMapper.convertValue(users.getData(), List.class);
            assertThat(data.size()).isEqualTo(3);
            assertThat(data.get(0)).isInstanceOf(Map.class);
            val user1 = (Map<String, String>) data.get(0);
            assertThat(user1.get("nameText")).isEqualTo("U1");
            assertThat(user1.get("name1Text")).isEqualTo("U1");
            assertThat(user1.get("rolesText")).isEqualTo("r1");
            assertThat(user1.get("typeText")).isEqualTo("Test1");
            val user2 = (Map<String, String>) data.get(1);
            assertThat(user2.get("nameText")).isEqualTo("U2");
            assertThat(user2.get("name1Text")).isEqualTo("U1");
            assertThat(user2.get("rolesText")).isEqualTo("r2");
            assertThat(user2.get("typeText")).isEqualTo("test3");

            verify(dictItemMapper, times(2)).getDictLabel(any(), any(), any(), any());
            verify(dictItemMapper, times(1)).getDictLabel(any(), any(), any(), any(), any());
            verify(upper, times(3)).transform(any(), anyBoolean());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGetUserPage() {
        // when
        val users = testController.getUserPage();
        // then
        val data = objectMapper.convertValue(users.getData().getRecords(), List.class);
        assertThat(data.size()).isEqualTo(3);
        assertThat(data.get(0)).isInstanceOf(Map.class);
        val user1 = (Map<String, String>) data.get(0);
        assertThat(user1.get("rolesText")).isEqualTo("r1");
        assertThat(user1.get("typeText")).isEqualTo("Test1");
        val user2 = (Map<String, String>) data.get(1);
        assertThat(user2.get("rolesText")).isEqualTo("r2");
        assertThat(user2.get("typeText")).isEqualTo("test3");

        verify(dictItemMapper, times(2)).getDictLabel(any(), any(), any(), any());
        verify(dictItemMapper, times(1)).getDictLabel(any(), any(), any(), any(), any());
    }

}

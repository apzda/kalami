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
package com.apzda.kalami.boot.controller;

import com.apzda.kalami.boot.TestApp;
import com.apzda.kalami.boot.autoconfig.KalamiXkaAutoConfiguration;
import com.apzda.kalami.boot.entity.User;
import com.apzda.kalami.mybatisplus.autoconfig.KalamiMyBatisPlusAutoConfiguration;
import com.apzda.kalami.security.autoconfig.KalamiSecurityAutoConfiguration;
import com.apzda.kalami.web.autoconfig.KalamiWebAutoConfiguration;
import com.baomidou.mybatisplus.test.autoconfigure.AutoConfigureMybatisPlus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@WebMvcTest(TestController.class)
@ContextConfiguration(classes = TestApp.class)
@ImportAutoConfiguration({ KalamiWebAutoConfiguration.class, KalamiXkaAutoConfiguration.class,
        KalamiMyBatisPlusAutoConfiguration.class, KalamiSecurityAutoConfiguration.class, AopAutoConfiguration.class })
@AutoConfigureMockMvc
@AutoConfigureMybatisPlus
@Sql(value = "classpath:/schema.sql")
@WithMockUser(authorities = "*:user.*")
@ComponentScan("com.apzda.kalami.boot.service")
public class TestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void test() throws Exception {
        mvc.perform(post("/user").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .content("pageSize=10&pageNumber=1&pageSorts=uid,createdAt|desc&id=gt 1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].id").value("2"))
            .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    @Order(2)
    void testGet() throws Exception {
        mvc.perform(get("/user/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("1"))
            .andExpect(jsonPath("$.data.type").value("1"))
            .andExpect(jsonPath("$.data.roles").value("1"))
            .andExpect(jsonPath("$.data.rolesText").value("r1"))
            .andExpect(jsonPath("$.data.typeText").value("Test1"));
    }

    @Test
    @Order(3)
    void testAdd() throws Exception {
        // given
        val user = new User();
        user.setId("user001");
        user.setName("Leo Ning");
        user.setRoles("1");
        user.setType("1");

        mvc.perform(put("/user").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("user001"))
            .andExpect(jsonPath("$.data.type").value("1"))
            .andExpect(jsonPath("$.data.roles").value("1"))
            .andExpect(jsonPath("$.data.rolesText").value("r1"))
            .andExpect(jsonPath("$.data.typeText").value("Test1"));
    }

    @Test
    @Order(4)
    void testUpdate() throws Exception {
        // given
        val user = new User();
        user.setName("Leo Ning");

        mvc.perform(patch("/user/1").contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("1"))
            .andExpect(jsonPath("$.data.name").value("Leo Ning"))
            .andExpect(jsonPath("$.data.type").value("1"))
            .andExpect(jsonPath("$.data.roles").value("1"))
            .andExpect(jsonPath("$.data.rolesText").value("r1"))
            .andExpect(jsonPath("$.data.typeText").value("Test1"));
    }

    @Test
    @Order(5)
    void testDelete() throws Exception {
        mvc.perform(delete("/user/100").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());

        mvc.perform(delete("/user/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("1"))
            .andExpect(jsonPath("$.data.name").value("u1"))
            .andExpect(jsonPath("$.data.type").value("1"))
            .andExpect(jsonPath("$.data.roles").value("1"))
            .andExpect(jsonPath("$.data.rolesText").value("r1"))
            .andExpect(jsonPath("$.data.typeText").value("Test1"));

        mvc.perform(get("/user/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
        mvc.perform(get("/user/2").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mvc.perform(get("/user/3").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void testDeleteBatch() throws Exception {

        mvc.perform(delete("/user").accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(List.of("1", "2"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").value("1"))
            .andExpect(jsonPath("$.data[0].name").value("u1"))
            .andExpect(jsonPath("$.data[0].type").value("1"))
            .andExpect(jsonPath("$.data[0].roles").value("1"))
            .andExpect(jsonPath("$.data[0].rolesText").value("r1"))
            .andExpect(jsonPath("$.data[0].typeText").value("Test1"))
            .andExpect(jsonPath("$.data[1].id").value("2"))
            .andExpect(jsonPath("$.data[1].name").value("u2"))
            .andExpect(jsonPath("$.data[1].roles").value("2"))
            .andExpect(jsonPath("$.data[1].rolesText").value("r2"));

        mvc.perform(get("/user/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());
        mvc.perform(get("/user/2").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound());

        mvc.perform(get("/user/3").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

}

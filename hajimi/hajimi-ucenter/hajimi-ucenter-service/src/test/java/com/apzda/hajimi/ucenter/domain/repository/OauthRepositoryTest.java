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
package com.apzda.hajimi.ucenter.domain.repository;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.apzda.hajimi.ucenter.app.TestApp;
import com.apzda.hajimi.ucenter.security.token.UCenterTokenManager;
import com.apzda.hajimi.ucenter.service.dto.OauthDto;
import com.apzda.kalami.mybatisplus.autoconfig.KalamiMyBatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

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
@ImportAutoConfiguration({ KalamiMyBatisPlusAutoConfiguration.class })
@ComponentScan({ "com.apzda.hajimi.ucenter.infrastructure" })
@MapperScan("com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper")
@Testcontainers(parallel = true)
@Sql(value = { "file:../schema/mysql/1.0.0.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = { "classpath:/reset.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OauthRepositoryTest {

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    UCenterTokenManager uCenterTokenManager;

    @Autowired
    OauthRepository oauthRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("全新注册")
    void createOauth() {
        // given
        OauthDto oauthDto = new OauthDto();
        oauthDto.setProvider("wx");
        oauthDto.setAppId("123");
        oauthDto.setOpenid("123");
        oauthDto.setPhone("13988888888");
        oauthDto.setSpm("a.b.c");
        oauthDto.setLoginTime(LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()));
        oauthDto.setDevice("ios");
        oauthDto.setIp("127.0.0.1");
        oauthDto.setUserAgent("test");

        // when
        val oauth = oauthRepository.createOauth(oauthDto);

        // then
        assertThat(oauth).isNotNull();

        val uid = oauth.getUid();
        val entity = userRepository.getById(uid);

        assertThat(entity).isNotNull();
    }

    @Test
    @Sql("classpath:oauth.sql")
    @DisplayName("OpenId已存在")
    void createOauthWhenExist() {
        // given
        OauthDto oauthDto = new OauthDto();
        oauthDto.setProvider("wx");
        oauthDto.setAppId("test");
        oauthDto.setOpenid("123");
        oauthDto.setPhone("13988888888");
        oauthDto.setSpm("a.b.c");
        oauthDto.setLoginTime(LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()));
        oauthDto.setDevice("ios");
        oauthDto.setIp("127.0.0.1");
        oauthDto.setUserAgent("test");

        // when
        val oauth = oauthRepository.createOauth(oauthDto);

        // then
        assertThat(oauth).isNotNull();
        assertThat(oauth.getId()).isEqualTo(2);
        assertThat(oauth.getUid()).isEqualTo(2);
    }

    @Test
    @Sql("classpath:oauth.sql")
    @DisplayName("UnionId已存在")
    void createOauthWhenUnionExist() {
        // given
        OauthDto oauthDto = new OauthDto();
        oauthDto.setProvider("wx");
        oauthDto.setAppId("789");
        oauthDto.setOpenid("def");
        oauthDto.setUnionid("456");
        oauthDto.setPhone("13988888888");
        oauthDto.setSpm("a.b.c");
        oauthDto.setLoginTime(LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()));
        oauthDto.setDevice("ios");
        oauthDto.setIp("127.0.0.1");
        oauthDto.setUserAgent("test");

        // when
        val oauth = oauthRepository.createOauth(oauthDto);

        // then
        assertThat(oauth).isNotNull();
        assertThat(oauth.getUid()).isEqualTo(2);
    }

}

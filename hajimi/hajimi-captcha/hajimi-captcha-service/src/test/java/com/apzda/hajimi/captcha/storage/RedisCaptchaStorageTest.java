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
package com.apzda.hajimi.captcha.storage;

import com.apzda.hajimi.captcha.Captcha;
import com.apzda.hajimi.captcha.TestApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@JsonTest
@ContextConfiguration(classes = TestApp.class)
@ImportAutoConfiguration(RedisAutoConfiguration.class)
@Disabled
class RedisCaptchaStorageTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void load() throws Exception {
        // given
        val captchaStorage = new RedisCaptchaStorage(stringRedisTemplate, objectMapper);
        String uuid = "123";
        val captcha = new Captcha();
        captcha.setId("12");
        // when
        val ca = captchaStorage.load(uuid, captcha);
        // then
        assertThat(ca).isNull();

        // given
        captcha.setExpireTime(System.currentTimeMillis() + Duration.ofSeconds(5).toMillis());
        captcha.setCode("7890");
        captchaStorage.save(uuid, captcha);

        // when
        val loaded = captchaStorage.load(uuid, captcha);
        // then
        assertThat(loaded).isNotNull();
        assertThat(loaded.getCode()).isEqualTo("7890");

        // when
        captchaStorage.remove(uuid, captcha);
        val loaded2 = captchaStorage.load(uuid, captcha);
        // then
        assertThat(loaded2).isNull();
    }

}

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
package com.apzda.kalami.redis.service;

import com.apzda.kalami.redis.TestConfig;
import com.apzda.kalami.redis.autoconfig.KalamiRedisAutoConfiguration;
import com.apzda.kalami.service.CounterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Slf4j
@JsonTest
@ContextConfiguration(
        classes = RedisBasedInfraServiceImplTest.class)
@ImportAutoConfiguration({ RedisAutoConfiguration.class, KalamiRedisAutoConfiguration.class })
@TestPropertySource(properties = { "spring.data.redis.database=1" })
@Import({ TestConfig.class })
class RedisBasedInfraServiceImplTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CounterService counterService;

    @Test
    void should_be_redis_infra_counter_service() {
        assertThat(counterService).isInstanceOf(RedisBasedInfraServiceImpl.class);
    }

    @Test
    void count() throws InterruptedException {
        // given
        CounterService counter = new RedisBasedInfraServiceImpl(stringRedisTemplate, objectMapper);
        val countDownLatch = new CountDownLatch(10);

        // when
        for (var i = 0; i < 10; i++) {
            new Thread(() -> {
                counter.count("abc", 5);
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        val count = counter.count("abc", 5);
        // then
        if (count != 1) {
            assertThat(count).isEqualTo(11);
        }
        // when
        TimeUnit.SECONDS.sleep(5);
        val count1 = counter.count("abc", 5);
        assertThat(count1).isEqualTo(1);
    }

    @Test
    void storage_should_be_worked() throws Exception {
        // given
        val storage = new RedisBasedInfraServiceImpl(stringRedisTemplate, objectMapper);
        val data = new TestData();
        data.setAge(18);
        data.setName("Leo Ning");
        // when
        val saved = storage.save("biz1.leo_ning", data);
        // then
        assertThat(saved).isNotNull();
        assertThat(saved).isSameAs(data);
        TimeUnit.SECONDS.sleep(2);
        assertThat(storage.getTtl("biz1.leo_ning").toSeconds()).isLessThan(5);
        // when
        val tData = storage.load("biz1.leo_ning", TestData.class);
        // then
        assertThat(tData).isPresent();
        assertThat(tData.get().getAge()).isEqualTo(18);
        assertThat(storage.exist("biz1.leo_ning")).isTrue();
        // when
        TimeUnit.SECONDS.sleep(6);
        val tData2 = storage.load("biz1.leo_ning", TestData.class);

        // then
        assertThat(tData2).isNotPresent();
        assertThat(storage.exist("biz1.leo_ning")).isFalse();
        val duration = storage.getTtl("biz1.leo_ning");
        assertThat(duration.toSeconds()).isLessThanOrEqualTo(0);
    }

    @Test
    void lock_should_be_worked() throws Exception {
        // given
        val storage = new RedisBasedInfraServiceImpl(stringRedisTemplate, objectMapper);

        // when
        val countDownLatch = new CountDownLatch(3);
        val error = new AtomicInteger();
        // when
        for (var i = 0; i < 3; i++) {
            new Thread(() -> {
                val testLock = storage.getLock("test_lock");
                try {
                    if (testLock.tryLock(20, TimeUnit.SECONDS)) {
                        log.info("done");
                    }
                    else {
                        throw new IllegalStateException("");
                    }
                    TimeUnit.MILLISECONDS.sleep(200);
                }
                catch (Exception e) {
                    error.incrementAndGet();
                }
                finally {
                    countDownLatch.countDown();
                    testLock.unlock();
                }
            }).start();
        }
        countDownLatch.await();
        // then
        assertThat(error.get()).isEqualTo(0);

        storage.deleteLock("test_lock");
    }

}

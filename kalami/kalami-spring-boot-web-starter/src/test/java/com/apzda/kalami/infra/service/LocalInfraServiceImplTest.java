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
package com.apzda.kalami.infra.service;

import com.apzda.kalami.infra.TestData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
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
class LocalInfraServiceImplTest {

    private static final LocalInfraServiceImpl LOCAL_INFRA_IMPL = new LocalInfraServiceImpl(Duration.ofDays(1));

    @Test
    void count() throws InterruptedException {
        // given
        val countDownLatch = new CountDownLatch(10);

        // when
        for (var i = 0; i < 10; i++) {
            new Thread(() -> {
                LOCAL_INFRA_IMPL.count("abc", 3);
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();
        val count = LOCAL_INFRA_IMPL.count("abc", 3);
        // then
        if (count != 1) {
            Assertions.assertThat(count).isEqualTo(11);
        }
    }

    @Test
    void storage_should_be_worked() throws Exception {
        // given
        val data = new TestData();
        data.setAge(18);
        data.setName("Leo Ning");
        // when
        val saved = LOCAL_INFRA_IMPL.save("biz1.leo_ning", data);
        // then
        assertThat(saved).isNotNull();
        assertThat(saved).isSameAs(data);
        TimeUnit.SECONDS.sleep(2);
        assertThat(LOCAL_INFRA_IMPL.getTtl("biz1.leo_ning").toSeconds()).isLessThan(5);
        // when
        val tData = LOCAL_INFRA_IMPL.load("biz1.leo_ning", TestData.class);
        // then
        assertThat(tData).isPresent();
        assertThat(tData.get().getAge()).isEqualTo(18);
        assertThat(LOCAL_INFRA_IMPL.exist("biz1.leo_ning")).isTrue();
        // when
        TimeUnit.SECONDS.sleep(10);
        val tData2 = LOCAL_INFRA_IMPL.load("biz1.leo_ning", TestData.class);

        // then
        assertThat(tData2).isNotPresent();
        assertThat(LOCAL_INFRA_IMPL.exist("biz1.leo_ning")).isFalse();
        val duration = LOCAL_INFRA_IMPL.getTtl("biz1.leo_ning");
        assertThat(duration.toSeconds()).isLessThanOrEqualTo(0);
    }

    @Test
    void lock_should_be_worked() throws Exception {
        // given
        val storage = LOCAL_INFRA_IMPL;

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

package com.apzda.kalami.redis.autoconfig;

import com.apzda.kalami.redis.service.RedisBasedInfraServiceImpl;
import com.apzda.kalami.service.CounterService;
import com.apzda.kalami.service.DistributedLockService;
import com.apzda.kalami.service.TempStorageService;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */

@ContextConfiguration(classes = KalamiRedisAutoConfigurationTest.class)
class KalamiRedisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("spring.data.redis.database=1")
        .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, RedisAutoConfiguration.class,
                KalamiRedisAutoConfiguration.class));

    @Test
    void test() {
        contextRunner.run(context -> {
            val counterService = context.getBean(CounterService.class);
            val tempStorageService = context.getBean(TempStorageService.class);
            val lockService = context.getBean(DistributedLockService.class);

            assertThat(counterService).isInstanceOf(RedisBasedInfraServiceImpl.class);
            assertThat(tempStorageService).isInstanceOf(RedisBasedInfraServiceImpl.class);
            assertThat(lockService).isInstanceOf(RedisBasedInfraServiceImpl.class);
        });
    }

}

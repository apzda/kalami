package com.apzda.kalami.web.autoconfig;

import com.apzda.kalami.infra.service.LocalInfraServiceImpl;
import com.apzda.kalami.service.CounterService;
import com.apzda.kalami.service.DistributedLockService;
import com.apzda.kalami.service.TempStorageService;
import com.apzda.kalami.web.advice.KalamiControllerAdvice;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@ContextConfiguration(classes = KalamiWebAutoConfigurationTest.class)
class KalamiWebAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("kalami.security.login-url=/login-p")
        .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class, WebMvcAutoConfiguration.class,
                KalamiInfraConfiguration.class, KalamiWebAutoConfiguration.class));

    @Test
    void t() {
        contextRunner.run(context -> {
            val advice = context.getBean(KalamiControllerAdvice.class);

            assertThat(advice.getLoginUrl()).isEqualTo("/login-p");

            val counterService = context.getBean(CounterService.class);
            val tempStorageService = context.getBean(TempStorageService.class);
            val lockService = context.getBean(DistributedLockService.class);
            val modemBean = context.containsBean("modem");
            assertThat(counterService).isInstanceOf(LocalInfraServiceImpl.class);
            assertThat(tempStorageService).isInstanceOf(LocalInfraServiceImpl.class);
            assertThat(lockService).isInstanceOf(LocalInfraServiceImpl.class);
            assertThat(modemBean).isTrue();
        });
    }

}

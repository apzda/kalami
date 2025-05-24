package com.apzda.kalami.i18n;

import com.apzda.kalami.autoconfig.KalamiCommonAutoConfiguration;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.MessageSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@SpringBootTest(classes = I18nTest.class)
class I18nTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(KalamiCommonAutoConfiguration.class));

    @Test
    void t() {
        contextRunner.withSystemProperties("app.locale=en_US").run(context -> {
            // when
            val bean = context.getBean(MessageSource.class);
            // then
            assertThat(bean).isNotNull();
            // when
            val err404 = I18n.t("error.404");
            // then
            assertThat(err404).isEqualTo("Not Found!!");

            // when
            val err405 = I18n.t("error.405");
            // then
            assertThat(err405).isEqualTo("Not Allowed!!");
        });
    }

}

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
import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
class LocalCaptchaStorageTest {

    @Test
    void local_storage_should_be_work_ok() throws Exception {
        // given
        val captchaStorage = new LocalCaptchaStorage(Duration.ofSeconds(10));
        String uuid = "123";
        val captcha = new Captcha();
        captcha.setId("12");
        // when
        val ca = captchaStorage.load(uuid, captcha);
        // then
        assertThat(ca).isNull();

        // given
        captcha.setExpireTime(5);
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
        assertThat(loaded2).isNull();
    }

}

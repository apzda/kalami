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
package com.apzda.hajimi.oss.hwobs.backend;

import com.apzda.hajimi.oss.config.BackendConfig;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Disabled
class HwObsBackendTest {

    private static HwObsBackend ossBackend;

    @BeforeAll
    static void init() {
        val config = new BackendConfig();
        config.setAccessKey("");
        config.setSecretKey("");
        config.setEndpoint("https://obs.cn-east-3.myhuaweicloud.com");
        config.setBucketName("zky-test-3e8e");
        config.setPathPatten("yyyy");
        ossBackend = new HwObsBackend();
        assertThat(ossBackend.init(config)).isTrue();
    }

    @Test
    void uploadFile() {

    }

}

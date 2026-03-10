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
package com.apzda.hajimi.oss.minio.backend;

import com.apzda.hajimi.oss.config.BackendConfig;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Disabled
class MinioBackendTest {

    private static MinioBackend ossBackend;

    @BeforeAll
    static void init() {
        val config = new BackendConfig();
        config.setAccessKey("fXqu6wy8ps2yAz8M");
        config.setSecretKey("ltFSRt20HzcHQ3qLfkG4Qhd9zKY6NANs");
        config.setEndpoint("http://127.0.0.1:39000");
        config.setBucketName("bucket-test");
        config.setPathPatten("yyyy");
        ossBackend = new MinioBackend();
        assertThat(ossBackend.init(config)).isTrue();
    }

    @Test
    void getFile() {
    }

    @Test
    void uploadFile() throws IOException {
        // given
        val file = new File("pom.xml");
        // when
        val fileInfo = ossBackend.uploadFile(file);
        // then
        assertThat(fileInfo).isNotNull();
        assertThat(fileInfo.getFilename()).isEqualTo("pom.xml");
        assertThat(fileInfo.getExt()).isEqualTo("xml");
    }

    @Test
    void delete() {
    }

}

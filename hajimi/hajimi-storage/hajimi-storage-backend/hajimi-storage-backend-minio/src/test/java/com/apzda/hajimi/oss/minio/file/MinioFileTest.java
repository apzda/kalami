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
package com.apzda.hajimi.oss.minio.file;

import com.apzda.hajimi.oss.config.BackendConfig;
import com.apzda.hajimi.oss.minio.backend.MinioBackend;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Disabled
class MinioFileTest {

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

    @AfterAll
    static void tearDown() {
        ossBackend.close();
    }

    @Test
    void getLocalFile() throws IOException {
        // given
        val file = new File("src/test/java/com/apzda/cloud/oss/minio/file/MinioFileTest.java");
        // when
        val fileInfo = ossBackend.uploadFile(file);
        // then
        assertThat(fileInfo).isNotNull();

        // given
        val path = fileInfo.getPath();
        val ossFile = new MinioFile(path, ossBackend);

        // when
        val stat = ossFile.stat();
        // then
        assertThat(stat).isNotNull();
        assertThat(stat.getFileId()).isEqualTo(fileInfo.getFileId());

        // when
        val localFile = ossFile.getLocalFile();
        // then
        assertThat(localFile).isNotNull();

        // given
        val content = FileCopyUtils.copyToString(new FileReader(localFile));
        // then
        assertThat(content).contains("bucket-test");

        // when
        val deleted = ossFile.delete();
        // then
        assertThat(deleted).isTrue();
    }

}

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
package com.apzda.hajimi.oss.fs.file;

import com.apzda.hajimi.oss.config.BackendConfig;
import com.apzda.hajimi.oss.fs.backend.FsBackend;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
class FsFileTest {

    @Test
    void fs_file_should_be_work() throws IOException {
        // given
        val config = new BackendConfig();
        config.setRootDir("./");
        config.setPathPatten("yyyy");
        val backend = new FsBackend();
        backend.init(config);
        val file = new FsFile("/pom.xml", backend);
        // when
        val stat = file.stat();
        // then
        assertThat(stat).isNotNull();
        assertThat(stat.getExt()).isEqualTo("xml");
        assertThat(stat.getExist()).isTrue();
        assertThat(stat.getFileId()).isNotBlank();
        assertThat(stat.getContentType()).isEqualTo("application/xml");
        // when
        val localFile = file.getLocalFile();
        assertThat(localFile).isNotNull();
        assertThat(localFile.exists()).isTrue();

        // when
        val inputStream = file.getInputStream();
        // then
        assertThat(inputStream).isNotNull();
        // when
        val string = FileCopyUtils.copyToString(new InputStreamReader(inputStream));
        // then
        assertThat(string).contains("hajimi-storage-backend-fs");

        inputStream.close();
    }

    @Test
    void file_should_exist() throws IOException {
        // given
        val file = new File("./test.txt");

        // when
        val exists = file.exists();
        // then
        assertThat(exists).isFalse();

        // when
        FileCopyUtils.copy("abc", new FileWriter("./test.txt"));

        // then
        assertThat(file.exists()).isTrue();
    }

    @AfterAll
    @BeforeAll
    static void tearDown() {
        val file = new File("./test.txt");
        if (file.exists()) {
            assertThat(file.delete()).isTrue();
        }
    }

}

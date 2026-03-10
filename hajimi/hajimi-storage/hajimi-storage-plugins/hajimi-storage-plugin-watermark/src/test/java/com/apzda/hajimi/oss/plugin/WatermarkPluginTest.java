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
package com.apzda.hajimi.oss.plugin;

import cn.hutool.core.io.FileUtil;
import com.apzda.hajimi.oss.config.BackendConfig;
import com.apzda.hajimi.oss.file.UploadedFile;
import com.apzda.hajimi.oss.fs.backend.FsBackend;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
class WatermarkPluginTest {

    @Test
    void alter() throws Exception {
        // given
        val ossBackend = new FsBackend();
        val backendConfig = new BackendConfig();
        backendConfig.setRootDir("./src/test/");
        ossBackend.init(backendConfig);
        val watermarkPlugin = new WatermarkPlugin();

        val builder = UploadedFile.builder();
        builder.file("./src/test/cats.jpg");
        builder.ext("jpg");
        val file = builder.build();
        val oriFile = file.getFile();

        val config = new HashMap<String, Object>();
        config.put(WatermarkPlugin.PROP_WATERMARK_S, "/watermark.png");
        config.put(WatermarkPlugin.PROP_TMPDIR_S, FileUtil.getTmpDirPath());
        config.put(WatermarkPlugin.PROP_KEEP_B, "true");
        config.put(WatermarkPlugin.PROP_NOISE_I, "0");
        config.put(WatermarkPlugin.PROP_POS_S, "rd");
        config.put(WatermarkPlugin.PROP_OPACITY_D, "0.55");
        config.put(WatermarkPlugin.PROP_ROTATE_I, "60");
        watermarkPlugin.config(config);
        // when
        val altered = watermarkPlugin.alter(file, null, ossBackend);

        // then
        val filePath = altered.getFile();
        assertThat(filePath).isNotBlank();
        assertThat(filePath).isNotEqualTo(oriFile);
    }

}

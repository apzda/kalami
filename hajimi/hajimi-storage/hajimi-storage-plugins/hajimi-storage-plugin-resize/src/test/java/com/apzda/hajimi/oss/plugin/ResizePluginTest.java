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
import com.apzda.hajimi.oss.file.UploadedFile;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
class ResizePluginTest {

    @Test
    void alter() throws Exception {
        // given
        val config = new HashMap<String, Object>();
        config.put(ResizePlugin.PROP_WIDTH_I, "120");
        config.put(ResizePlugin.PROP_TMPDIR_S, FileUtil.getTmpDirPath());
        config.put(ResizePlugin.PROP_KEEP_B, "true");
        val resizePlugin = new ResizePlugin();
        resizePlugin.config(config);
        val builder = UploadedFile.builder();
        builder.file("./src/test/cat.jpeg");
        builder.ext("jpeg");
        val file = builder.build();
        // when
        val altered = resizePlugin.alter(file, "/", null);
        // then
        val image = ImageIO.read(new File(altered.getFile()));
        val width = image.getWidth();
        val height = image.getHeight();
        assertThat(width).isEqualTo(120);
        assertThat(height).isEqualTo((120 * 183 / 276));
    }

    @Test
    void supported() {
        // given
        val resizePlugin = new ResizePlugin();
        val ext = "png";
        val ext1 = "exe";
        // when
        val supported = resizePlugin.supported(ext);
        val supported1 = resizePlugin.supported(ext1);
        // then
        assertThat(supported).isTrue();
        assertThat(supported1).isFalse();
    }

}

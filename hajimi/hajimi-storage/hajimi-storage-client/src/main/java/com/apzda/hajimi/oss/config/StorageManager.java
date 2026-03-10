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
package com.apzda.hajimi.oss.config;

import com.apzda.hajimi.oss.backend.OssBackend;
import com.apzda.hajimi.oss.file.IOssFile;
import com.apzda.hajimi.oss.file.StorageFile;
import jakarta.annotation.Nonnull;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
public abstract class StorageManager {

    private static Lazy<OssBackend> ossBackend;

    private static Lazy<StorageClientAutoConfiguration> configuration;

    StorageManager(ApplicationContext context) {
        if (ossBackend == null) {
            ossBackend = Lazy.of(() -> context.getBean(StorageClientAutoConfiguration.class).getBackend());
        }
        configuration = Lazy.of(() -> context.getBean(StorageClientAutoConfiguration.class));
    }

    @Nonnull
    public static String getRealBackend(String backend) {
        return configuration.get().theRealBackend(backend);
    }

    @Nonnull
    public static OssBackend getOssBackend() {
        return ossBackend.get();
    }

    @Nonnull
    public static OssBackend getOssBackend(String backend) {
        return configuration.get().getBackend(backend);
    }

    @Nonnull
    public static IOssFile createFile(@Nonnull String fileName) {
        return new StorageFile(fileName);
    }

    @Nonnull
    public static IOssFile createFile(@Nonnull String fileName, String backend) {
        return new StorageFile(fileName, backend);
    }

}

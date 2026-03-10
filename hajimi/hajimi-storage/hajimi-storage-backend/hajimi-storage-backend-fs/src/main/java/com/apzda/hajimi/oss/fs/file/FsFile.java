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

import cn.hutool.core.io.FileUtil;
import com.apzda.hajimi.oss.config.BackendConfig;
import com.apzda.hajimi.oss.file.FileInfo;
import com.apzda.hajimi.oss.file.IOssFile;
import com.apzda.hajimi.oss.fs.backend.FsBackend;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
public class FsFile implements IOssFile {

    private final String filePath;

    private final File file;

    protected final BackendConfig config;

    protected final String baseUrl;

    protected final String params;

    protected final String rootDir;

    public FsFile(@Nonnull String filePath, @Nonnull FsBackend backend) {
        if (filePath.startsWith("/")) {
            this.filePath = filePath;
        }
        else {
            this.filePath = "/" + filePath;
        }
        this.config = backend.getConfig();
        this.baseUrl = config.getBaseUrl();
        this.params = config.getParams();
        this.rootDir = config.getRootDir();
        this.file = new File(rootDir + filePath);
    }

    @Override
    public File getLocalFile() throws IOException {
        if (file.exists()) {
            return file;
        }
        else {
            throw new FileNotFoundException(rootDir + filePath);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    @Override
    public FileInfo stat() throws IOException {
        val builder = FileInfo.builder();
        builder.exist(file.exists());
        builder.backend("fs");
        if (file.exists()) {
            builder.error(0);
            builder.path(filePath);
            builder.url(getFullUrl(baseUrl, filePath, params));
            builder.length(file.length());
            builder.ext(FileUtil.extName(file));
            builder.filename(file.getName());
            builder.contentType(URLConnection.guessContentTypeFromName(file.getName()));
            try (val input = new FileInputStream(file)) {
                builder.fileId(DigestUtils.md5DigestAsHex(input));
                FileTime creationTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                builder.createTime(creationTime.toMillis());
            }
            catch (FileNotFoundException e) {
                throw e;
            }
            catch (IOException e) {
                builder.createTime(0);
            }
            return builder.build();
        }
        else {
            throw new FileNotFoundException("File not found: " + filePath);
        }
    }

    @Override
    public boolean delete() throws IOException {
        return Files.deleteIfExists(file.toPath());
    }

}

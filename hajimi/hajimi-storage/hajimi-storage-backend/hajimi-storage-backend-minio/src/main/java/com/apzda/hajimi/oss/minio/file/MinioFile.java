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

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.apzda.hajimi.oss.config.BackendConfig;
import com.apzda.hajimi.oss.file.FileInfo;
import com.apzda.hajimi.oss.file.IOssFile;
import com.apzda.hajimi.oss.minio.backend.MinioBackend;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URLConnection;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
public class MinioFile implements IOssFile {

    private final String filePath;

    private final String objectName;

    private final BackendConfig config;

    private final MinioClient ossClient;

    private final MinioBackend backend;

    public MinioFile(@Nonnull String path, @Nonnull MinioBackend backend) throws IOException {
        if (path.startsWith("/")) {
            this.filePath = path;
            this.objectName = path.substring(1);
        }
        else {
            this.filePath = "/" + path;
            this.objectName = path;
        }
        this.backend = backend;
        this.config = backend.getConfig();
        this.ossClient = backend.getOssClient();
        if (this.ossClient == null) {
            throw new IOException("MinIO Client is not initialized");
        }
    }

    @Override
    public File getLocalFile() throws IOException {
        val tmpDir = config.getTmpDir();
        val stat = stat();
        val localFileName = tmpDir + SecureUtil.md5(stat.getFileId()) + "." + stat.getExt();
        var localFile = new File(localFileName);
        if (localFile.exists()) {
            return localFile;
        }
        synchronized (filePath) {
            if (localFile.exists()) {
                return localFile;
            }
            FileCopyUtils.copy(getInputStream(), new BufferedOutputStream(new FileOutputStream(localFile)));
            return localFile;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        val bucketName = config.getBucketName();
        val builder = GetObjectArgs.builder();
        val arg = builder.bucket(bucketName).object(objectName).build();
        try {
            val ossObject = ossClient.getObject(arg);
            return new BufferedInputStream(ossObject);
        }
        catch (IOException ie) {
            throw ie;
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public FileInfo stat() throws IOException {
        try {
            val argBuilder = StatObjectArgs.builder();
            val args = argBuilder.bucket(config.getBucketName()).object(objectName).build();
            val meta = ossClient.statObject(args);
            val userMeta = meta.userMetadata();
            val filename = userMeta.getOrDefault("filename", FileUtil.getName(filePath));
            val builder = FileInfo.builder();
            builder.error(0);
            builder.exist(true);
            builder.path(filePath);
            builder.url(theUrl(filePath));
            builder.length(meta.size());
            builder.fileId(meta.etag());
            builder.filename(filename);
            builder.contentType(URLConnection.guessContentTypeFromName(filename));
            builder.ext(FileUtil.extName(filename));
            builder.createTime(Long.parseLong(userMeta.getOrDefault("createtime", "0")));
            builder.backend("minio");
            return builder.build();
        }
        catch (Exception e) {
            throw new FileNotFoundException(filePath + ": " + e.getMessage());
        }
    }

    @Override
    public boolean delete() throws IOException {
        return backend.delete(filePath);
    }

    private String theUrl(String filePath) {
        var baseUrl = config.getBaseUrl();
        if (StringUtils.isBlank(baseUrl)) {
            val bucketName = config.getBucketName();
            val endpoint = config.getEndpoint();
            baseUrl = StringUtils.stripEnd(endpoint, "/") + "/" + bucketName;
        }

        return getFullUrl(baseUrl, filePath, config.getParams());
    }

}

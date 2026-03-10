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
package com.apzda.hajimi.oss.hwobs.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.apzda.hajimi.oss.config.BackendConfig;
import com.apzda.hajimi.oss.file.FileInfo;
import com.apzda.hajimi.oss.file.IOssFile;
import com.apzda.hajimi.oss.hwobs.backend.HwObsBackend;
import com.obs.services.ObsClient;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.net.URLConnection;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class HwObsFile implements IOssFile {

    private final String filePath;

    private final String objectName;

    private final BackendConfig config;

    private final ObsClient ossClient;

    private final HwObsBackend backend;

    public HwObsFile(@Nonnull String path, @Nonnull HwObsBackend backend) throws IOException {
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
            throw new IOException("Huawei OBS Client is not initialized");
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
        val ossObject = ossClient.getObject(bucketName, objectName);

        return new BufferedInputStream(ossObject.getObjectContent());
    }

    @Override
    public FileInfo stat() throws IOException {
        try {
            val meta = ossClient.getObjectMetadata(config.getBucketName(), objectName);
            val userMeta = meta.getAllMetadata();
            val filename = (String) userMeta.getOrDefault("filename", FileUtil.getName(filePath));
            val builder = FileInfo.builder();
            builder.error(0);
            builder.exist(true);
            builder.path(filePath);
            builder.url(theUrl(filePath));
            builder.length(meta.getContentLength());
            builder.fileId(meta.getContentMd5());
            builder.filename(filename);
            builder.ext(FileUtil.extName(filename));
            builder.contentType(URLConnection.guessContentTypeFromName(filename));
            builder.createTime(Long.parseLong((String) userMeta.getOrDefault("createtime", "0")));
            builder.backend("hwobs");

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

    @Nonnull
    private String theUrl(String filePath) {
        var baseUrl = config.getBaseUrl();
        if (StringUtils.isBlank(baseUrl)) {
            val bucketName = config.getBucketName();
            val endpoint = config.getEndpoint();
            baseUrl = "https://" + bucketName + "." + endpoint;
        }

        return getFullUrl(baseUrl, filePath, config.getParams());
    }

}

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

import com.apzda.hajimi.oss.backend.OssBackend;
import com.apzda.hajimi.oss.config.BackendConfig;
import com.apzda.hajimi.oss.file.FileInfo;
import com.apzda.hajimi.oss.file.IOssFile;
import com.apzda.hajimi.oss.hwobs.file.HwObsFile;
import com.obs.services.ObsClient;
import com.obs.services.model.ObjectMetadata;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Getter
public class HwObsBackend implements OssBackend {

    private BackendConfig config;

    private String bucketName;

    private ObsClient ossClient;

    @Override
    public boolean init(@Nonnull BackendConfig config) {
        this.config = config;
        this.bucketName = config.getBucketName();
        try {
            val endpoint = "https://" + config.getEndpoint();
            val accessKey = config.getAccessKey();
            val secretKey = config.getSecretKey();
            this.ossClient = new ObsClient(accessKey, secretKey, endpoint);
            return true;
        }
        catch (Exception e) {
            log.error("Cannot initialize Huawei OBS Client: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public IOssFile getFile(String filePath) throws IOException {
        if (ossClient == null) {
            throw new IOException("Huawei OBS Client is not initialized");
        }

        return new HwObsFile(filePath, this);
    }

    @Override
    public FileInfo uploadFile(File file, String path) throws IOException {
        return uploadFile(new FileInputStream(file), file.getName(), path);
    }

    @Override
    public FileInfo uploadFile(InputStream stream, String fileName, String path) throws IOException {
        if (ossClient == null) {
            throw new IOException("Huawei OBS Client is not initialized");
        }

        try (val bs = new BufferedInputStream(stream)) {
            val filePath = generatePath(fileName, config.getPathPatten(), path);
            val meta = new ObjectMetadata();
            meta.addUserMetadata("filename", URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            meta.addUserMetadata("createtime", String.valueOf(System.currentTimeMillis()));
            val result = ossClient.putObject(config.getBucketName(), filePath.substring(1), bs, meta);
            if (result == null) {
                throw new IOException("Cannot upload file: response is null");
            }

            val ossFile = getFile(filePath);
            return ossFile.stat();
        }
    }

    @Override
    public boolean delete(String filePath) throws IOException {
        if (ossClient == null) {
            throw new IOException("Huawei OBS Client is not initialized");
        }
        val result = ossClient.deleteObject(config.getBucketName(), filePath.substring(1));

        return result != null && result.getStatusCode() == 204;
    }

}

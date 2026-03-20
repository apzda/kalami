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
package com.apzda.hajimi.oss.controller.api;

import cn.hutool.core.io.FileUtil;
import com.apzda.hajimi.oss.backend.OssBackend;
import com.apzda.hajimi.oss.cache.FileInfoCache;
import com.apzda.hajimi.oss.client.dto.ChunkUploadDto;
import com.apzda.hajimi.oss.client.dto.UploadDto;
import com.apzda.hajimi.oss.client.vo.ChunkUploadVo;
import com.apzda.hajimi.oss.config.StorageConfigProperties;
import com.apzda.hajimi.oss.config.StorageManager;
import com.apzda.hajimi.oss.config.StorageServerConfigProperties;
import com.apzda.hajimi.oss.exception.ChunkNotFoundException;
import com.apzda.hajimi.oss.exception.FileExtNameNotAllowedException;
import com.apzda.hajimi.oss.exception.FileSizeNotAllowedException;
import com.apzda.hajimi.oss.file.FileInfo;
import com.apzda.hajimi.oss.file.UploadedFile;
import com.apzda.hajimi.oss.resumable.ResumableInfo;
import com.apzda.hajimi.oss.resumable.ResumableStorage;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.http.Base64DecodeMultipartFile;
import com.google.common.base.Splitter;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController("sysOssStorageController")
@RequestMapping("/oss/")
@RequiredArgsConstructor
public class StorageController implements InitializingBean {

    private final StorageConfigProperties properties;

    private final StorageServerConfigProperties serviceProperties;

    private final FileInfoCache fileInfoCache;

    private List<StorageServerConfigProperties.PluginConfig> plugins;

    @Override
    public void afterPropertiesSet() throws Exception {
        plugins = serviceProperties.getPlugins();
    }

    /**
     * 文件上传
     */
    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<List<FileInfo>> upload(UploadDto request, MultipartFile uploadFile) {
        List<FileInfo> fileInfos = new ArrayList<>();
        val backend = StorageManager.getRealBackend(request.getBackend());
        val content = request.getContent();
        val files = request.getFiles();
        if (!uploadFile.isEmpty()) {
            // 单一文件
            val fb = UploadedFile.builder();
            try {
                val originalFilename = uploadFile.getOriginalFilename();
                val fileName = StringUtils.defaultIfBlank(originalFilename, uploadFile.getName());
                fb.contentType(Objects.requireNonNull(uploadFile.getContentType()));
                fb.filename(fileName);
                fb.name(StringUtils.defaultIfBlank(request.getName(), uploadFile.getName()));
                fb.size(uploadFile.getSize());
                fb.ext(FileUtil.extName(fileName));
                val tmpFile = File.createTempFile("UP_LD_", ".part");
                fb.file(tmpFile.getAbsolutePath());
                uploadFile.transferTo(tmpFile);
            }
            catch (IOException e) {
                fb.error(e.getMessage());
            }
            files.add(fb.build());
        }
        else if (StringUtils.isNotBlank(content)) {
            // base64编码的图片上传
            val fb = UploadedFile.builder();
            try {
                val mFile = Base64DecodeMultipartFile.base64Convert(content);
                val fileName = StringUtils.defaultIfBlank(mFile.getOriginalFilename(), mFile.getName());
                fb.contentType(Objects.requireNonNull(mFile.getContentType()));
                fb.filename(fileName);
                fb.name(StringUtils.defaultIfBlank(request.getName(), mFile.getName()));
                fb.size(mFile.getSize());
                fb.ext(FileUtil.extName(fileName));
                val tmpFile = File.createTempFile("UP_LD_", ".part");
                fb.file(tmpFile.getAbsolutePath());
                mFile.transferTo(tmpFile);
            }
            catch (IOException e) {
                fb.error(e.getMessage());
            }
            files.add(fb.build());
        }
        else {
            return Response.error(400);
        }

        val ossBackend = StorageManager.getOssBackend(backend);
        val fileCount = files.size();
        val path = request.getPath();
        log.debug("Start dealing file upload: fileCount = {}, path = {}, backend = {}", fileCount, path, backend);

        if (fileCount > 0) {
            val futures = new ArrayList<CompletableFuture<FileInfo>>();
            val plugins = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(request.getPlugins());
            for (int i = 0; i < fileCount; i++) {
                val index = i;
                CompletableFuture<FileInfo> uploaded = CompletableFuture.supplyAsync(() -> {
                    UploadedFile file = files.get(index);
                    FileInfo fileInfo;
                    val filename = file.getFilename();
                    try {
                        checkFileValid(file);
                        file = applyPlugins(file, path, ossBackend, plugins);
                        val tmpFilePath = file.getFile();
                        fileInfo = ossBackend.uploadFile(new FileInputStream(tmpFilePath), filename, path);
                        log.debug("Upload success: fileName = {}, path = {}, size = {}", filename, fileInfo.getPath(),
                                DataSize.ofBytes(fileInfo.getLength()));
                    }
                    catch (Exception e) {
                        fileInfo = FileInfo.builder().error(1).message(e.getMessage()).build();
                        log.error("Upload fail: fileName = {}", filename, e);
                    }
                    finally {
                        try {
                            if (StringUtils.isNotBlank(file.getFile())) {
                                val f = new File(file.getFile());
                                if (f.exists()) {
                                    val deleted = f.delete();
                                    log.debug("Delete the original file: {} - {}", deleted, f);
                                }
                            }
                        }
                        catch (Exception e) {
                            log.warn("Cannot delete tmp file: {}", e.getMessage());
                        }
                    }
                    fileInfo.setFilename(filename);
                    fileInfo.setIndex(index);
                    return fileInfo;
                });

                futures.add(uploaded);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[fileCount])).join();

            for (CompletableFuture<FileInfo> future : futures) {
                try {
                    fileInfos.add(future.get());
                }
                catch (Exception e) {
                    log.warn("file upload error !!!", e);
                }
            }
        }

        if (fileInfos.isEmpty()) {
            return Response.error(400);
        }

        return Response.success(fileInfos);
    }

    /**
     * 分片上传
     */
    @PostMapping(value = "/chunk", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<ChunkUploadVo> chunkUpload(ChunkUploadDto request, MultipartFile uploadFile) throws IOException {
        if (uploadFile.isEmpty()) {
            return chunkCheck(request);
        }
        val backend = StorageManager.getRealBackend(request.getBackend());
        val chunkNumber = request.getChunkNumber();
        val resumableStorage = ResumableStorage.getInstance();
        val info = resumableStorage.getResumableInfo(request, properties.getTmpDir());
        val fb = UploadedFile.builder();
        fb.name(uploadFile.getOriginalFilename());
        fb.filename(request.getFileName());
        fb.size(uploadFile.getSize());
        fb.contentType(uploadFile.getContentType());
        fb.ext(FileUtil.extName(request.getFileName()));
        request.setFile(fb.build());

        val file = request.getFile();
        val path = request.getPath();
        if (log.isDebugEnabled()) {
            log.debug("Start dealing file chunk-upload: ResumableInfo = {}, backend = {}", info, backend);
        }

        if (info.resumableTotalSize > serviceProperties.getMaxFileSize().toBytes()) {
            log.warn("The size of {} is larger than {}", request.getFileName(), serviceProperties.getMaxFileSize());
            throw new FileSizeNotAllowedException("The size is larger than " + serviceProperties.getMaxFileSize());
        }

        checkFileValid(file);
        try (val is = uploadFile.getInputStream(); val raf = new RandomAccessFile(info.resumableFilePath, "rw")) {
            // Seek to position
            raf.seek((chunkNumber - 1) * (long) info.resumableChunkSize);
            long readed = 0;
            long contentLength = file.getSize();
            byte[] bytes = new byte[1024 * 100];
            while (readed < contentLength) {
                int r = is.read(bytes);
                if (r < 0) {
                    break;
                }
                raf.write(bytes, 0, r);
                readed += r;
            }
            raf.close();

            info.uploadedChunks.add(new ResumableInfo.ResumableChunkNumber(chunkNumber));

            val fInfo = FileInfo.builder();
            fInfo.fileId(info.resumableIdentifier);
            fInfo.error(0);
            fInfo.backend(backend);
            fInfo.length(contentLength);
            fInfo.ext(file.getExt());
            fInfo.filename(request.getFileName());
            fInfo.contentType(file.getContentType());
            fInfo.message(file.getError());

            val vo = new ChunkUploadVo();
            vo.setChunkNumber(chunkNumber);
            vo.setFile(fInfo.build());

            if (info.checkIfUploadFinished()) {
                // Check if all chunks uploaded, and change filename
                ResumableStorage.getInstance().remove(info);

                CompletableFuture.runAsync(() -> {
                    if (log.isDebugEnabled()) {
                        log.debug("All chunks are uploaded, now save it to backend[{}]: {}", backend, info);
                    }

                    try {
                        val ossBackend = StorageManager.getOssBackend(backend);
                        val disables = Splitter.on(",")
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(request.getDisables());
                        val fBuilder = UploadedFile.builder();
                        fBuilder.file(info.resumableFilePath);
                        fBuilder.size(info.resumableTotalSize);
                        val uploadedFile = applyPlugins(fBuilder.build(), path, ossBackend, disables);
                        info.resumableFilePath = uploadedFile.getFile();
                        try (val fileStream = new FileInputStream(info.resumableFilePath)) {
                            val fileInfo = ossBackend.uploadFile(fileStream, info.resumableFilename, path);
                            fileInfoCache.setFileInfo(info.resumableIdentifier, fileInfo);
                            if (log.isDebugEnabled()) {
                                log.debug("File saved to backend[{}]: {}", backend, info);
                            }
                        }
                    }
                    catch (Exception e) {
                        log.error("Cannot save file to backend: {} - {}", info, e.getMessage());
                        val fileInfo = FileInfo.builder();
                        fileInfo.error(1);
                        fileInfo.message(e.getMessage());
                        fileInfoCache.setFileInfo(info.resumableIdentifier, fileInfo.build());
                    }
                    finally {
                        val f = new File(info.resumableFilePath);
                        if (f.exists()) {
                            val deleted = f.delete();
                            log.debug("Delete the resumable file: {} - {}", deleted, f);
                        }
                    }
                });
            }

            return Response.success(vo);
        }
        catch (FileNotFoundException fne) {
            log.error("File not found: {} - {}", info, fne.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fne.getMessage());
        }
        catch (IOException e) {
            log.error("File cannot upload: {} - {}", info, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 检查分片信息
     */
    @PostMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<ChunkUploadVo> chunkCheck(@RequestBody ChunkUploadDto request) {
        int chunkNumber = request.getChunkNumber();

        val resumableStorage = ResumableStorage.getInstance();
        val info = resumableStorage.getResumableInfo(request, properties.getTmpDir());

        if (info.uploadedChunks.contains(new ResumableInfo.ResumableChunkNumber(chunkNumber))) {
            val vo = new ChunkUploadVo();
            vo.setChunkNumber(chunkNumber);
            return Response.success(vo);
        }
        else {
            throw new ChunkNotFoundException();
        }
    }

    /**
     * 查询文件信息
     */
    @GetMapping(value = "/query/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<ChunkUploadVo> query(@PathVariable("fileId") String fileId) {
        val fileInfo = fileInfoCache.getFileInfo(fileId);
        val vo = new ChunkUploadVo();
        if (fileInfo != null) {
            vo.setFile(fileInfo);
        }
        return Response.success(vo);
    }

    private void checkFileValid(UploadedFile file) {
        try {
            val error = file.getError();
            if (StringUtils.isNotBlank(error)) {
                throw new IllegalStateException(error);
            }
            val size = file.getSize();
            if (size == 0) {
                log.warn("The size of {} is 0", file.getFilename());
                throw new FileSizeNotAllowedException("The size is 0");
            }
            if (size > serviceProperties.getMaxFileSize().toBytes()) {
                throw new FileSizeNotAllowedException("The size is larger than " + serviceProperties.getMaxFileSize());
            }
            val fileTypes = serviceProperties.getFileTypes();
            val ext = file.getExt();
            if (StringUtils.isBlank(ext) || !fileTypes.contains(ext.toLowerCase())) {
                log.warn("The extension of {} is now allowed: {}", file.getFilename(), ext);
                throw new FileExtNameNotAllowedException("The extension is now allowed: " + ext);
            }
        }
        catch (Exception e) {
            val f = new File(file.getFile());
            if (f.exists()) {
                val deleted = f.delete();
                log.debug("Delete the original part file: {} - {}", deleted, f);
            }
            throw e;
        }
    }

    private UploadedFile applyPlugins(@Nonnull UploadedFile file, String path, OssBackend ossBackend,
            List<String> enabledPlugins) throws Exception {

        val ext = file.getExt();
        UploadedFile alteredFile = file;
        log.debug("Apply plugins for '{}': {}", file.getFilename(), enabledPlugins);
        for (val plugin : plugins) {
            if (enabledPlugins.contains(plugin.getId()) && plugin.getFileTypes().contains(ext)
                    && plugin.instance().supported(ext)) {
                alteredFile = plugin.instance().alter(alteredFile, path, ossBackend);
            }
        }
        return alteredFile;
    }

}

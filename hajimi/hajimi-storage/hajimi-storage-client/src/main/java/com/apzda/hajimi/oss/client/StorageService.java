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
package com.apzda.hajimi.oss.client;

import com.apzda.hajimi.oss.client.dto.UploadDto;
import com.apzda.hajimi.oss.file.FileInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
//@formatter:off
@FeignClient(
    name = "${kalami.cloud.feign.service.mashup.name:mashupSvc}",
    url = "${kalami.cloud.feign.service.mashup.url:}",
    contextId = "kalami.cloud.feign.service.StorageService",
    primary = false
)
//@formatter:on
public interface StorageService {

    /**
     * 文件上传
     */
    @PostMapping(value = "/_/storageSvc/upload", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<FileInfo> upload(UploadDto request, MultipartFile uploadFile);

}

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
package com.apzda.hajimi.oss.client.dto;

import com.apzda.hajimi.oss.file.UploadedFile;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class ChunkUploadDto {

    @NotBlank
    private String fileName;

    @Min(1)
    private Integer totalChunks;

    @Min(1)
    private Integer chunkNumber;

    @Min(1)
    @NotNull
    private Integer chunkSize;

    @Min(1)
    @NotNull
    private Long totalSize;

    private String fileId;

    private String path;

    private String bucket;

    private String disables;

    private UploadedFile file;

    private String backend;

}

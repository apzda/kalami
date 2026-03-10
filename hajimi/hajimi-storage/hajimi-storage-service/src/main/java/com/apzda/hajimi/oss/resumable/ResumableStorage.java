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
package com.apzda.hajimi.oss.resumable;

import com.apzda.hajimi.oss.client.dto.ChunkUploadDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.HashMap;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class ResumableStorage {

    private ResumableStorage() {
    }

    private static ResumableStorage sInstance;

    public static synchronized ResumableStorage getInstance() {
        if (sInstance == null) {
            sInstance = new ResumableStorage();
        }
        return sInstance;
    }

    private final HashMap<String, ResumableInfo> mMap = new HashMap<>();

    public synchronized ResumableInfo get(int resumableChunkSize, long resumableTotalSize, String resumableIdentifier,
            String resumableFilename, String resumableFilePath) {

        ResumableInfo info = mMap.get(resumableIdentifier);

        if (info == null) {
            info = new ResumableInfo();

            info.resumableChunkSize = resumableChunkSize;
            info.resumableTotalSize = resumableTotalSize;
            info.resumableIdentifier = resumableIdentifier;
            info.resumableFilename = resumableFilename;
            info.resumableFilePath = resumableFilePath;

            mMap.put(resumableIdentifier, info);
        }
        return info;
    }

    public void remove(ResumableInfo info) {
        mMap.remove(info.resumableIdentifier);
    }

    public ResumableInfo getResumableInfo(ChunkUploadDto req, String baseDir) {

        int resumableChunkSize = req.getChunkSize();
        long resumableTotalSize = req.getTotalSize();
        String resumableIdentifier = req.getFileId();
        String resumableFilename = req.getFileName();
        String resumableFilePath = new File(baseDir, resumableFilename).getAbsolutePath() + ".temp";

        ResumableInfo info = get(resumableChunkSize, resumableTotalSize, resumableIdentifier, resumableFilename,
                resumableFilePath);

        if (!info.valid()) {
            remove(info);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Invalid request params: chunkSize=%d, totalSize=%d, fileId=%s,fileName=%s ",
                            resumableChunkSize, resumableTotalSize, resumableIdentifier, resumableFilename));
        }
        return info;
    }

}

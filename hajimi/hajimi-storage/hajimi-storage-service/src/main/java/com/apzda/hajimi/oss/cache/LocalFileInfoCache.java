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
package com.apzda.hajimi.oss.cache;

import com.apzda.hajimi.oss.file.FileInfo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class LocalFileInfoCache implements FileInfoCache {

    private final Cache<String, FileInfo> cache;

    public LocalFileInfoCache() {
        cache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(7200)).build();
    }

    @Override
    public FileInfo getFileInfo(String fileId) {
        return cache.getIfPresent(fileId);
    }

    @Override
    public void setFileInfo(String fileId, FileInfo info) {
        cache.put(fileId, info);
    }

    @Override
    public void remove(String fileId) {
        cache.invalidate(fileId);
    }

}

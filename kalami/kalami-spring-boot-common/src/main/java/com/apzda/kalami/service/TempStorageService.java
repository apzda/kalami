/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.service;

import com.apzda.kalami.data.TempData;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public interface TempStorageService {

    @Nullable
    <T extends TempData> T save(@NonNull String id, @NonNull T data) throws Exception;

    @NonNull
    <T extends TempData> Optional<T> load(@NonNull String id, @NonNull Class<T> tClass);

    @NonNull
    @SuppressWarnings("unchecked")
    default <T extends TempData> T load(@NonNull String id, @NonNull T defaultValue) {
        return load(id, defaultValue.getClass()).map(expiredData -> (T) expiredData).orElse(defaultValue);
    }

    boolean exist(@NonNull String id);

    void remove(@NonNull String id);

    void expire(@NonNull String id, Duration duration);

    @NonNull
    Duration getTtl(@NonNull String id);

}

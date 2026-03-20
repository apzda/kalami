/*
 * Copyright 2023-2026 the original author or authors.
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
import jakarta.annotation.Nonnull;
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
    <T extends TempData> T save(@Nonnull String id, @Nonnull T data) throws Exception;

    @Nonnull
    <T extends TempData> Optional<T> load(@Nonnull String id, @Nonnull Class<T> tClass);

    @Nonnull
    @SuppressWarnings("unchecked")
    default <T extends TempData> T load(@Nonnull String id, @Nonnull T defaultValue) {
        return load(id, defaultValue.getClass()).map(expiredData -> (T) expiredData).orElse(defaultValue);
    }

    boolean exist(@Nonnull String id);

    void remove(@Nonnull String id);

    void expire(@Nonnull String id, Duration duration);

    @Nonnull
    Duration getTtl(@Nonnull String id);

}

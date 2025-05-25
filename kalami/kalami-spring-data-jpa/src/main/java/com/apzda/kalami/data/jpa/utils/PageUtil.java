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
package com.apzda.kalami.data.jpa.utils;

import com.apzda.kalami.data.Paged;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public abstract class PageUtil {

    @Nonnull
    public static <T> Paged<T> from(Page<T> page) {
        if (page == null) {
            return new Paged<>();
        }
        return from(page, page.getContent());
    }

    @Nonnull
    public static <T> Paged<T> from(@Nonnull Page<?> page, @Nonnull List<T> records) {
        val result = new Paged<T>();
        result.setCurrent(page.getNumber());
        result.setPages(page.getTotalPages());
        result.setTotal(page.getTotalElements());
        result.setSize(page.getSize());

        result.setRecords(records);

        return result;
    }

    @Nonnull
    public static <T, E> Paged<T> from(@Nonnull Page<E> page, @Nonnull List<E> records,
                                       @Nonnull Function<? super List<E>, ? extends List<T>> converter) {
        val result = new Paged<T>();
        result.setCurrent(page.getNumber());
        result.setPages(page.getTotalPages());
        result.setTotal(page.getTotalElements());
        result.setSize(page.getSize());
        result.setRecords(converter.apply(records));

        return result;
    }

}

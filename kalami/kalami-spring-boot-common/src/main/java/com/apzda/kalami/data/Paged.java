/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.data;

import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.val;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * 分页数据
 *
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class Paged<T> {

    /**
     * 当前页码
     */
    private long current;

    /**
     * 分页大小
     */
    private long size;

    /**
     * 数据总数。-1表示未计算总数
     */
    private long total = -1;

    /**
     * 总页数。-1表示未计算总页数
     */
    private long pages = -1;

    /**
     * 数据集
     */
    private List<T> records = Collections.emptyList();

    public Paged() {
    }

    public Paged(@Nonnull List<T> records) {
        this.records = records;
    }

    public Paged(@Nonnull Page<T> page) {
        current = page.getNumber();
        size = page.getSize();
        total = page.getTotalElements();
        pages = page.getTotalPages();
        records = page.getContent();
    }

    public List<T> getRecords() {
        if (records == null) {
            return Collections.emptyList();
        }
        return records;
    }

    @Nonnull
    public static <T, E> Paged<T> of(@Nonnull Page<E> page,
            @Nonnull Function<? super List<E>, ? extends List<T>> mapper) {
        val paged = new Paged<>(mapper.apply(page.getContent()));
        paged.current = page.getNumber();
        paged.size = page.getSize();
        paged.total = page.getTotalElements();
        paged.pages = page.getTotalPages();

        return paged;
    }

}

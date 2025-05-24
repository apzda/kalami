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
package com.apzda.kalami.mybatisplus.utils;

import com.apzda.kalami.data.Paged;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public abstract class PageUtil {

    @Nonnull
    public static <T> Paged<T> from(IPage<T> page) {
        if (page == null) {
            return new Paged<>();
        }
        return from(page, page.getRecords());
    }

    @Nonnull
    public static <T> Paged<T> from(@Nonnull IPage<?> page, @Nonnull List<T> records) {
        val result = new Paged<T>();
        result.setCurrent(page.getCurrent());
        result.setPages(page.getPages());
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());

        result.setRecords(records);

        return result;
    }

    @Nonnull
    public static <T, E> Paged<T> from(@Nonnull IPage<E> page, @Nonnull List<E> records,
            @Nonnull Function<? super List<E>, ? extends List<T>> converter) {
        val result = new Paged<T>();
        result.setCurrent(page.getCurrent());
        result.setPages(page.getPages());
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        result.setRecords(converter.apply(records));

        return result;
    }

    @Nonnull
    public static <T> IPage<T> from(@Nonnull Pageable pageable) {
        val page = new Page<T>(pageable.getPageNumber(), pageable.getPageSize());
        val sort = pageable.getSort();

        sort.forEach(sortable -> {
            val field = sortable.getProperty();
            val direction = sortable.getDirection();
            val orderItem = new OrderItem();
            orderItem.setColumn(field);
            orderItem.setAsc(direction == Sort.Direction.ASC);

            page.addOrder(orderItem);
        });

        return page;
    }

}

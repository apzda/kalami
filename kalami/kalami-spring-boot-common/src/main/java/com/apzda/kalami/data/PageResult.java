/*
 * Copyright 2025 the original author or authors.
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

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface PageResult<T> {

    /**
     * 当前页码
     */
    long getCurrent();

    long getSize();

    /**
     * 数据总数。-1表示未计算总数
     */
    long getTotal();

    /**
     * 总页数。-1表示未计算总页数
     */
    long getPages();

    @Nonnull
    List<T> getRecords();

}

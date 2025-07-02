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
package com.apzda.kalami.utils;

import com.apzda.kalami.data.ITree;
import jakarta.annotation.Nonnull;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public abstract class TreeUtils {

    @Nonnull
    public static <T> List<T> getChildIds(@Nonnull ITree<T> parent) {
        return getChildIds(parent.getChildren(), 0);
    }

    @Nonnull
    public static <T> List<T> getAllChildrenIds(@Nonnull ITree<T> parent) {
        return getChildIds(parent.getChildren(), Integer.MAX_VALUE);
    }

    @Nonnull
    public static <T> List<T> getChildIds(@Nonnull ITree<T> parent, int level) {
        return getChildIds(parent.getChildren(), level);
    }

    @Nonnull
    public static <T> List<T> getChildIds(List<? extends ITree<T>> children) {
        return getChildIds(children, 0);
    }

    @Nonnull
    public static <T> List<T> getAllChildrenIds(List<? extends ITree<T>> children) {
        return getChildIds(children, Integer.MAX_VALUE);
    }

    @Nonnull
    public static <T> List<T> getChildIds(List<? extends ITree<T>> children, int level) {
        Set<T> ids = new HashSet<>();
        if (!CollectionUtils.isEmpty(children)) {
            children.forEach(child -> {
                if (ids.add(child.getId()) && level > 0) {
                    getChildIds(ids, child.getChildren(), 0, level);
                }
            });
        }
        return ids.stream().toList();
    }

    public static <T> boolean isMyChild(T id, @Nonnull ITree<T> parent) {
        return getAllChildrenIds(parent.getChildren()).contains(id);
    }

    public static <T> boolean isMyChild(T id, @Nonnull List<? extends ITree<T>> children) {
        return getAllChildrenIds(children).contains(id);
    }

    private static <T> void getChildIds(Set<T> ids, List<? extends ITree<T>> children, int level, int maxLevel) {
        if (!CollectionUtils.isEmpty(children) && level < maxLevel) {
            children.forEach(child -> {
                if (ids.add(child.getId()) && (level + 1) < maxLevel) {
                    getChildIds(ids, child.getChildren(), level + 1, maxLevel);
                }
            });
        }
    }

}

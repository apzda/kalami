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
package com.apzda.kalami.boot.utils;

import com.apzda.kalami.boot.aop.DictionaryAdvisor;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public abstract class DictionaryUtils {

    private static Lazy<DictionaryAdvisor> lazy;

    public DictionaryUtils(ApplicationContext applicationContext) {
        if (lazy == null) {
            lazy = Lazy.of(() -> {
                try {
                    return applicationContext.getBean(DictionaryAdvisor.class);
                }
                catch (Exception e) {
                    log.error("Initialize DictionaryUtils failed: {}", e.getMessage());
                    return null;
                }
            });
        }
    }

    public static Object fill(@Nullable Object data) {
        if (data == null) {
            return null;
        }
        val opt = lazy.getOptional();
        if (opt.isPresent()) {
            return opt.get().fill(data);
        }
        return data;
    }

}

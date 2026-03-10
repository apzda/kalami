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
package com.apzda.hajimi.configure;

import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
public interface Setting extends Serializable {

    @Nonnull
    static String genSettingKey(@Nonnull Class<? extends Setting> sClass, String tenantId) {
        return sClass.getCanonicalName() + "@" + StringUtils.defaultIfBlank(tenantId, "0");
    }

    String name();

    default String icon() {
        return null;
    }

    default String style() {
        return null;
    }

}

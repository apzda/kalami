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
package com.apzda.kalami.boot.query;

import com.apzda.kalami.utils.StringUtil;
import jakarta.annotation.Nullable;
import lombok.Getter;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Getter
public enum MatchTypeEnum {

    /** 查询链接规则 AND */
    AND("AND"),
    /** 查询链接规则 OR */
    OR("OR");

    private final String value;

    MatchTypeEnum(String value) {
        this.value = value;
    }

    @Nullable
    public static MatchTypeEnum getByValue(Object value) {
        if (StringUtil.isEmpty(value)) {
            return null;
        }
        return getByValue(value.toString());
    }

    @Nullable
    public static MatchTypeEnum getByValue(String value) {
        if (StringUtil.isEmpty(value)) {
            return null;
        }
        for (MatchTypeEnum val : values()) {
            if (val.getValue().equalsIgnoreCase(value)) {
                return val;
            }
        }
        return null;
    }

}

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
package com.apzda.kalami.jackson;

import lombok.Getter;

/**
 * 样例字典枚举
 *
 * @author john <service@cheerel.com>
 */
@Getter
public enum ExampleDictEnum implements DictEnum {

    Y("启用"), N("禁用");

    private final String text;

    ExampleDictEnum(String text) {
        this.text = text;
    }

    @Override
    public Object getValue() {
        return this.name();
    }

}

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

import java.util.ArrayList;
import java.util.List;

/**
 * 样例字典
 * <p>
 * 用于通用查询字典列表：入参 @RequestParam ProductDict dict；返回dict.queryDict()；
 *
 * @author john <service@cheerel.com>
 */
@Getter
public enum ExampleDict {

    ExampleDictEnum("样例字典枚举", ExampleDictEnum.class);

    /**
     * 描述
     */
    private final String desc;

    /**
     * 字典枚举类
     */
    private final Class<? extends Enum<? extends DictEnum>> dictEnumClass;

    ExampleDict(String desc, Class<? extends Enum<? extends DictEnum>> dictEnumClass) {
        this.desc = desc;
        this.dictEnumClass = dictEnumClass;
    }

    /**
     * 字典查询
     */
    public List<DictEnumPrinter> queryDict() {
        List<DictEnumPrinter> result = new ArrayList<>();
        for (Enum<? extends DictEnum> enumConstant : this.dictEnumClass.getEnumConstants()) {
            DictEnum dictEnum = (DictEnum) enumConstant;
            result.add(new DictEnumPrinter(dictEnum.getText(), dictEnum.getValue()));
        }
        return result;
    }

}

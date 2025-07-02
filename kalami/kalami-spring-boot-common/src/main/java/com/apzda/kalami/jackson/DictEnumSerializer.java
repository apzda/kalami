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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;

/**
 * 字典枚举序列化器
 * <p>
 * 在枚举类字段上加注解 @JsonSerialize(using = DictEnumSerializer.class)，则自动增加以"Text"结尾的新属性字段，并设置值为字典的getText()方法返回值
 * <p>
 * 枚举类必须实现了{@link DictEnum 字典枚举接口}，才可生效
 *
 * @author john <service@cheerel.com>
 */
public class DictEnumSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private BeanProperty beanProperty;

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        // 写入原始字段
        gen.writeString(value.toString());

        // 新增结尾为"Text"的属性，并设置值为字典的getText()方法返回值
        if (value instanceof DictEnum enumValue) {
            gen.writeFieldName(beanProperty.getName() + "Text");
            gen.writeString(enumValue.getText());
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
        this.beanProperty = beanProperty;
        return this;
    }

}

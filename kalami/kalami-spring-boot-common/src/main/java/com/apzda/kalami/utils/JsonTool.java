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

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.apzda.kalami.jackson.JacksonCustomizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Jackson工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonTool {

    private static final ObjectMapper OBJECT_MAPPER = defaultObjectMapper();

    public static ObjectMapper defaultObjectMapper() {
        try {
            return ComTool.getBean(ObjectMapper.class);
        }
        catch (Exception e) {
            return new JacksonCustomizer().getObjectMapper();
        }
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 创建对象节点
     */
    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    /**
     * 创建数组节点
     */
    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 将对象转换为JSON格式的字符串
     * @param object 要转换的对象
     * @return JSON格式的字符串，如果对象为null，则返回null
     * @throws RuntimeException 如果转换过程中发生JSON处理异常，则抛出运行时异常
     */
    public static String toJsonString(Object object) {
        if (ObjectUtil.isNull(object)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型的对象
     * @param text JSON格式的字符串
     * @return 转换后的对象，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static JsonNode parseObject(String text) {
        if (ComTool.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(text);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型的对象
     * @param text JSON格式的字符串
     * @param clazz 要转换的目标对象类型
     * @param <T> 目标对象的泛型类型
     * @return 转换后的对象，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (ComTool.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字节数组转换为指定类型的对象
     * @param bytes 字节数组
     * @param clazz 要转换的目标对象类型
     * @param <T> 目标对象的泛型类型
     * @return 转换后的对象，如果字节数组为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(bytes, clazz);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型的对象，支持复杂类型
     * @param text JSON格式的字符串
     * @param typeReference 指定类型的TypeReference对象
     * @param <T> 目标对象的泛型类型
     * @return 转换后的对象，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        if (ComTool.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, typeReference);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为Dict对象
     * @param text JSON格式的字符串
     * @return 转换后的Dict对象，如果字符串为空或者不是JSON格式则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static Dict parseMap(String text) {
        if (ComTool.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructType(Dict.class));
        }
        catch (MismatchedInputException e) {
            // 类型不匹配说明不是json
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为Dict对象的列表
     * @param text JSON格式的字符串
     * @return 转换后的Dict对象的列表，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static List<Dict> parseArrayMap(String text) {
        if (ComTool.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Dict.class));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型对象的列表
     * @param text JSON格式的字符串
     * @param clazz 要转换的目标对象类型
     * @param <T> 目标对象的泛型类型
     * @return 转换后的对象的列表，如果字符串为空则返回空列表
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        if (ComTool.isBlank(text)) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(text,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

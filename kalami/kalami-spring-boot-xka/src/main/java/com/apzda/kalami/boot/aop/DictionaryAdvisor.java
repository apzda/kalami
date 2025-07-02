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
package com.apzda.kalami.boot.aop;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.EnumUtil;
import com.apzda.kalami.boot.config.XkaBootConfigProperties;
import com.apzda.kalami.boot.dict.DictItem;
import com.apzda.kalami.boot.dict.TransformUtils;
import com.apzda.kalami.boot.mapper.DictItemMapper;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.dictionary.Dict;
import com.apzda.kalami.utils.SanitizeUtils;
import com.apzda.kalami.utils.StringUtil;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.apzda.kalami.utils.SanitizeUtils.sanitize;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Aspect
@Component
@Order
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "kalami.boot.dictionary.enabled", havingValue = "true", matchIfMissing = true)
public class DictionaryAdvisor implements ApplicationContextAware {

    private final static ThreadLocal<Map<String, Map<String, Object>>> caches = new ThreadLocal<>();

    private final static ThreadLocal<Map<Enum<?>, Object>> enumCaches = new ThreadLocal<>();

    private final XkaBootConfigProperties properties;

    private final DictItemMapper dictItemMapper;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        TransformUtils.setApplicationContext(applicationContext);
        SanitizeUtils.setApplicationContext(applicationContext);
    }

    @Pointcut("execution(@com.apzda.kalami.annotation.Dictionary public com.apzda.kalami.data.Response *(..))")
    public void dictionaryPointcut() {
    }

    @Around("dictionaryPointcut()")
    Object dictionaryAdvice(@Nonnull ProceedingJoinPoint pjp) throws Throwable {
        val returnObj = pjp.proceed();

        if (returnObj instanceof Response<?> response) {
            try {
                caches.set(new HashMap<>());
                enumCaches.set(new HashMap<>());
                val data = response.getData();
                val realReturn = new Response<>();
                realReturn.setErrCode(response.getErrCode());
                realReturn.setErrMsg(response.getErrMsg());
                realReturn.setType(response.getType());
                realReturn.setErrType(response.getType());
                realReturn.setHttpCode(response.getHttpCode());

                if (data instanceof Collection<?> collection) {
                    realReturn.setData(collection.stream().map(this::fill).toList());
                }
                else if (data instanceof IPage<?> page) {
                    val newPage = Page.of(page.getCurrent(), page.getSize(), page.getTotal(), page.searchCount());
                    newPage.setRecords(page.getRecords().stream().map(this::fill).toList());
                    realReturn.setData(newPage);
                }
                else if (data instanceof Paged<?> page) {
                    val newPage = Page.of(page.getCurrent(), page.getSize(), page.getTotal(), true);
                    newPage.setRecords(page.getRecords().stream().map(this::fill).toList());
                    realReturn.setData(newPage);
                }
                else if (data != null && !BeanUtils.isSimpleProperty(data.getClass())) {
                    realReturn.setData(fill(data));
                }

                if (realReturn.getData() != null) {
                    return realReturn;
                }
            }
            finally {
                caches.remove();
                enumCaches.remove();
            }
        }

        return returnObj;
    }

    @Nullable
    public Object fill(@Nullable Object data) {
        if (data == null) {
            return null;
        }

        if (BeanUtils.isSimpleProperty(data.getClass())) {
            return data;
        }
        val map = new HashMap<String, Object>();

        val properties = BeanUtil.getPropertyDescriptorMap(data.getClass(), false);
        if (CollectionUtils.isEmpty(properties)) {
            return data;
        }

        val fields = com.apzda.kalami.utils.BeanUtils.getAllFieldsMap(data);

        for (Map.Entry<String, PropertyDescriptor> property : properties.entrySet()) {
            val name = property.getKey();
            val pd = property.getValue();
            val method = pd.getReadMethod();
            if (method != null) {
                try {
                    val field = fields.get(name);
                    val value = sanitize(field, method, method.invoke(data));
                    if (value == null) {
                        continue;
                    }
                    map.put(name, value);
                    fillDict(field, method, name, value, map);
                }
                catch (IllegalAccessException | InvocationTargetException e) {
                    log.warn("Cannot get value of property [{}] from [{}]", name, data.getClass());
                }
            }
        }

        return map;
    }

    @SuppressWarnings("unchecked")
    private void fillDict(Field field, @Nonnull Method method, String name, Object value,
            @Nonnull HashMap<String, Object> map) {
        Dict annotation = method.getAnnotation(Dict.class);
        if (annotation == null && field != null) {
            annotation = field.getAnnotation(Dict.class);
        }

        if (annotation == null) {
            return;
        }
        val dictFieldName = name + StringUtils.defaultIfBlank(this.properties.getLabelSuffix(), "Text");
        val label = annotation.value();
        val transformerClz = annotation.transformer();
        val object = "*".equals(label);
        if (!transformerClz.isInterface()) {
            val cache = caches.get().computeIfAbsent("transformer." + transformerClz, (key) -> new HashMap<>());
            val dictVal = cache.computeIfAbsent(value.toString(), (key) -> {
                val transformer = TransformUtils.getTransformer(transformerClz);
                if (transformer != null) {
                    return transformer.transform(value, object);
                }
                return null;
            });
            map.put(dictFieldName, dictVal);
            return;
        }

        var table = annotation.table();
        if (StringUtils.isBlank(table)) {
            val entity = annotation.entity();
            if (entity.isAnnotationPresent(TableName.class)) {
                val ann = entity.getAnnotation(TableName.class);
                table = ann.value();
            }
        }
        val realTable = table;
        val code = StringUtils.defaultIfBlank(annotation.code(), "id");

        if (EnumUtil.isEnum(value)) {
            val dictText = enumCaches.get().computeIfAbsent((Enum<?>) value, (v) -> getTextFromEnum(v, label));
            map.put(dictFieldName, dictText);
        }
        else if (StringUtils.isNotBlank(table)) {
            val cache = caches.get().computeIfAbsent(table + "." + code, (key) -> new HashMap<>());
            val dictText = cache.computeIfAbsent(value.toString(), (key) -> {
                if ("*".equals(label) || StringUtils.isBlank(label)) {
                    return getRowFromTable(realTable, code, key);
                }
                else {
                    return getTextFromTable(realTable, code, key, label);
                }
            });
            map.put(dictFieldName, dictText);
        }
        else if (StringUtils.isNotBlank(code)) {
            val cache = caches.get().computeIfAbsent("." + code, (key) -> {
                val kv = new HashMap<String, Object>();
                val items = getTextFromDictItemTable(code);
                for (DictItem item : items) {
                    kv.put(item.getVal(), item.getLabel());
                }
                return kv;
            });
            map.put(dictFieldName, cache.get(value.toString()));
        }
        else if (StringUtils.isNotBlank(label)) {
            map.put(dictFieldName, label);
        }
    }

    private List<DictItem> getTextFromDictItemTable(String code) {
        val config = properties;
        val dictDelColumn = config.getDeletedColumn();
        val dictNotDeletedValue = config.getNotDeletedValue();
        val dictLabelColumn = config.getLabelColumn();
        if (StringUtils.isNotBlank(dictDelColumn)) {
            return dictItemMapper.getDictLabel(config.getTableName(), config.getCodeColumn(), code,
                    config.getValueColumn(), dictDelColumn, dictNotDeletedValue, dictLabelColumn);
        }
        else {
            return dictItemMapper.getDictLabel(config.getTableName(), config.getCodeColumn(), code,
                    config.getValueColumn(), dictLabelColumn);
        }
    }

    private String getTextFromTable(String table, String code, @Nonnull Object value, String label) {
        return dictItemMapper.getDictLabel(table, code, label, value.toString());
    }

    @Nonnull
    private Map<String, Object> getRowFromTable(String table, String code, @Nonnull Object value) {
        val row = dictItemMapper.getDictRow(table, code, value.toString());
        if (CollectionUtils.isEmpty(row)) {
            return Collections.emptyMap();
        }

        Map<String, Object> map = new HashMap<>(row.size());
        row.forEach((k, v) -> {
            map.put(StringUtil.toCamel(k), v);
        });
        return map;
    }

    private static Object getTextFromEnum(@Nonnull Enum<?> value, String label) {
        val type = value.getClass();
        val fields = com.apzda.kalami.utils.BeanUtils.getAllFieldsMap(value);
        for (Map.Entry<String, Field> kv : fields.entrySet()) {
            val name = kv.getKey();
            val field = kv.getValue();

            if (name.equals(label) || field.getAnnotation(JsonPropertyDescription.class) != null) {
                val pd = BeanUtil.getPropertyDescriptor(type, name);
                val method = pd.getReadMethod();
                if (method != null) {
                    try {
                        return method.invoke(value);
                    }
                    catch (IllegalAccessException | InvocationTargetException e) {
                        log.warn("Cannot get value of property [{}] from enum [{}]", name, value.getClass());
                    }
                }
            }
        }

        return EnumUtil.toString(value);
    }

}

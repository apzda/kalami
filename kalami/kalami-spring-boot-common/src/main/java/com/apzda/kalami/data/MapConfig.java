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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;

import java.util.Map;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class MapConfig<C> {

    private static final Logger log = LoggerFactory.getLogger(MapConfig.class);

    private final Map<String, Object> props;

    private final Class<C> clazz;

    @SuppressWarnings("unchecked")
    public MapConfig(Map<String, Object> props) {
        this.props = props;
        val resolvableType = ResolvableType.forClass(MapConfig.class, this.getClass());
        this.clazz = (Class<C>) resolvableType.getGeneric(0).resolve();
    }

    public String getString(String name) {
        return Optional.ofNullable(this.props.get(name)).map(Object::toString).orElse(null);
    }

    public String getString(String name, String defaultValue) {
        return Optional.ofNullable(this.props.get(name)).map(Object::toString).orElse(defaultValue);
    }

    public C get(String name) {
        val prop = this.props.get(name);
        if (prop instanceof Map<?, ?> mapProp) {
            return BeanUtil.copyProperties(mapProp, this.clazz);
        }

        throw new IllegalArgumentException(StrUtil.format("[{}] cannot convert to {}", prop, this.clazz));
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }

    public int getInt(String name, int defValue) {
        String value = getString(name);
        if (value == null || value.isBlank()) {
            return defValue;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (Exception e) {
            log.warn("the {}({}) is not a valid int value, use {} as default", name, value, defValue);
            return defValue;
        }
    }

    public long getLong(String name) {
        return getLong(name, 0);
    }

    public long getLong(String name, long defValue) {
        String value = getString(name);
        if (value == null || value.isBlank()) {
            return defValue;
        }
        try {
            return Long.parseLong(value);
        }
        catch (Exception e) {
            log.warn("the {}({}) is not a valid long value, use {} as default", name, value, defValue);
            return defValue;
        }
    }

    public double getDouble(String name) {
        return getDouble(name, 0);
    }

    public double getDouble(String name, double defValue) {
        String value = getString(name);
        if (value == null || value.isBlank()) {
            return defValue;
        }
        try {
            return Double.parseDouble(value);
        }
        catch (Exception e) {
            log.warn("the {}({}) is not a valid double value, use {} as default", name, value, defValue);
            return defValue;
        }
    }

    public float getFloat(String name) {
        return getFloat(name, 0);
    }

    public float getFloat(String name, float defValue) {
        String value = getString(name);
        if (value == null || value.isBlank()) {
            return defValue;
        }
        try {
            return Float.parseFloat(value);
        }
        catch (Exception e) {
            log.warn("the {}({}) is not a valid float value, use {} as default", name, value, defValue);
            return defValue;
        }
    }

    public boolean getBool(String name) {
        return getBool(name, false);
    }

    public boolean getBool(String name, boolean defValue) {
        String value = getString(name);
        if (value == null || value.isBlank()) {
            return defValue;
        }
        return Boolean.parseBoolean(value);
    }

}

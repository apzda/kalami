/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author fengz (windywany@gmail.com)
 * @since 2025/05/15
 * @version 1.0.0
 */
public abstract class StringUtil {

    public static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_([a-z])");

    public static final Pattern CAMEL_PATTERN = Pattern.compile("([A-Z])");

    public static final String STRING_NULL = "null";

    public static String toUnderscore(String str) {
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            return str;
        }
        return CAMEL_PATTERN.matcher(lowerFirst(str)).replaceAll(m -> "_" + m.group(1).toLowerCase());
    }

    public static String camelToUnderline(String str) {
        return toUnderscore(str);
    }

    public static String toDashed(String str) {
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            return str;
        }
        return CAMEL_PATTERN.matcher(lowerFirst(str)).replaceAll(m -> "-" + m.group(1).toLowerCase());
    }

    public static String toCamel(String underscoreStr) {
        if (org.apache.commons.lang3.StringUtils.isBlank(underscoreStr)) {
            return underscoreStr;
        }

        return UNDERSCORE_PATTERN.matcher(lowerFirst(underscoreStr)).replaceAll(m -> m.group(1).toUpperCase());
    }

    public static String lowerFirst(String str) {
        if (org.apache.commons.lang3.StringUtils.isBlank(str)) {
            return str;
        }
        return org.apache.commons.lang3.StringUtils.lowerCase(String.valueOf(str.charAt(0))) + str.substring(1);
    }

    public static boolean isEmpty(Object object) {
        if (object == null) {
            return (true);
        }
        if ("".equals(object)) {
            return (true);
        }
        return "null".equals(object);
    }

    public static boolean isNotEmpty(Object object) {
        return object != null && !"".equals(object) && !STRING_NULL.equals(object);
    }

    @Nullable
    public static String convert(byte[] bytes, Charset charset) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, Optional.ofNullable(charset).orElse(StandardCharsets.UTF_8));
    }

    @Nonnull
    public static String convert(String source, Charset charset) {
        if (StringUtils.isBlank(source)) {
            return source;
        }

        return convert(source.getBytes(StandardCharsets.UTF_8), charset);
    }

    @Nonnull
    public static String convert(String source, @Nonnull Charset sourceCharset, Charset charset) {
        if (StringUtils.isBlank(source) || sourceCharset.equals(charset)) {
            return source;
        }

        return convert(source.getBytes(sourceCharset), charset);
    }

    @Nullable
    public static String convert(byte[] bytes, @Nonnull Charset sourceCharset, Charset charset) {
        if (bytes == null) {
            return null;
        }
        if (sourceCharset.equals(charset)) {
            return new String(bytes, sourceCharset);
        }

        return convert(new String(bytes, sourceCharset), charset);
    }

}

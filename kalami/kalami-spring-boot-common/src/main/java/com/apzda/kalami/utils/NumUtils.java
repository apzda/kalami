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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public abstract class NumUtils {

    private final static String digits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final static char[] digitChars = digits.toCharArray();

    private final static BigDecimal CURRENT_NULL_RATIO = BigDecimal.valueOf(-100);

    private final static BigDecimal PREVIOUS_NULL_RATIO = BigDecimal.valueOf(100);

    /**
     * 将Long类型的数字转为36进制的字符串
     */
    @Nonnull
    public static String toBase36(Long number) {
        if (number == null) {
            return "";
        }

        if (number < 0) {
            throw new IllegalArgumentException("数字必须非负");
        }

        if (number == 0) {
            return "0";
        }

        StringBuilder base36 = new StringBuilder();
        while (number > 0) {
            int remainder = (int) (number % 36);
            base36.insert(0, digitChars[remainder]);
            number /= 36;
        }

        return base36.toString();
    }

    /**
     * 将36进制数字转为Long
     */
    @Nullable
    public static Long fromBase36(String number) {
        if (number == null || number.isEmpty()) {
            return null;
        }

        long result = 0;

        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            int index = digits.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("非法的36进制字符: " + c);
            }
            result = result * 36 + index;
        }

        return result;
    }

    /**
     * 计算增长比例。
     * @param current 值
     * @param previous 基值
     * @return 比例
     */
    @Nonnull
    public static Double growthRatio(Integer current, Integer previous, int scale) {
        if (current == null) {
            return -100.0d;
        }
        if (previous == null || previous == 0) {
            return 100.0d;
        }

        return BigDecimal.valueOf((current - previous) * 100.0 / previous)
            .round(MathContext.UNLIMITED)
            .setScale(scale, RoundingMode.DOWN)
            .doubleValue();
    }

    /**
     * 计算增长比例。
     * @param current 值
     * @param previous 基值
     * @return 比例
     */
    @Nonnull
    public static Double growthRatio(Long current, Long previous, int scale) {
        if (current == null) {
            return -100.0d;
        }
        if (previous == null || previous == 0) {
            return 100.0d;
        }

        return BigDecimal.valueOf((current - previous) * 100.0 / previous)
            .round(MathContext.UNLIMITED)
            .setScale(scale, RoundingMode.DOWN)
            .doubleValue();
    }

    /**
     * 计算增长百分比。
     * @param current 值
     * @param previous 基值
     * @return 百分比
     */
    @Nonnull
    public static BigDecimal growthRatio(Double current, Double previous, int scale) {
        if (current == null) {
            return CURRENT_NULL_RATIO;
        }
        if (previous == null || previous == 0) {
            return PREVIOUS_NULL_RATIO;
        }

        return BigDecimal.valueOf((current - previous) * 100.0 / previous)
            .round(MathContext.UNLIMITED)
            .setScale(scale, RoundingMode.DOWN);
    }

    /**
     * 计算增长百分比。
     * @param current 值
     * @param previous 基值
     * @return 百分比
     */
    @Nonnull
    public static BigDecimal growthRatio(BigDecimal current, BigDecimal previous, int scale) {
        if (current == null) {
            return CURRENT_NULL_RATIO;
        }
        if (previous == null || previous.signum() == 0) {
            return PREVIOUS_NULL_RATIO;
        }

        return current.subtract(previous).movePointRight(2).divide(previous, scale, RoundingMode.DOWN);
    }

}

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

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.apzda.kalami.data.enums.DatePeriod;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.val;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public abstract class DateUtils {

    /**
     * 将时间调到<code>date</code>所在天开始时间
     *
     * @param date 日期
     * @return 一天的开始时间
     */
    @Nullable
    public static LocalDateTime beginOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * 将时间调到<code>date</code>所在天的结束时间
     * @param date 日期
     * @return 一天的结束时间
     */
    @Nullable
    public static LocalDateTime endOfDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atTime(23, 59, 59).withNano(0);
    }

    /**
     * 将时间调到<code>date</code>所在天开始时间
     *
     * @param date 日期
     * @return 一天的开始时间
     */
    @Nullable
    public static LocalDateTime beginOfDay(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * 将时间调到<code>date</code>所在天的结束时间
     *
     * @param date 日期
     * @return 一天的结束时间
     */
    @Nullable
    public static LocalDateTime endOfDay(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        return date.withHour(23).withMinute(59).withSecond(59).withNano(0);
    }

    /**
     * 将时间调到<code>date</code>所在天开始时间
     *
     * @param date 日期
     * @return 一天的开始时间
     */
    @Nullable
    public static LocalDateTime beginOfDay(Date date) {
        if (date == null) {
            return null;
        }

        return DateUtils.beginOfDay(DateUtil.toLocalDateTime(date));
    }

    /**
     * 将时间调到<code>date</code>所在天的结束时间
     *
     * @param date 日期
     * @return 一天的结束时间
     */
    @Nullable
    public static LocalDateTime endOfDay(Date date) {
        if (date == null) {
            return null;
        }

        return DateUtils.endOfDay(DateUtil.toLocalDateTime(date));
    }

    /**
     * Long类型时间转为{@link LocalDateTime}<br>
     * 只支持毫秒级别时间戳，如果需要秒级别时间戳，请自行×1000
     *
     * @param instant Long类型Date（Unix时间戳）
     * @return 一天的开始时间
     */
    public static LocalDateTime beginOfDay(long instant) {
        return beginOfDay(DateUtil.date(instant).toLocalDateTime());
    }

    /**
     * Long类型时间转为{@link LocalDateTime}<br>
     * 只支持毫秒级别时间戳，如果需要秒级别时间戳，请自行×1000
     *
     * @param instant Long类型Date（Unix时间戳）
     * @return 一天的结束时间
     */
    public static LocalDateTime endOfDay(long instant) {
        return endOfDay(DateUtil.date(instant).toLocalDateTime());
    }

    /**
     * 将dateTime转换为时间戳
     *
     * @param dateTime 时间
     * @return 时间戳
     */
    public static Long toEpochMilli(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LocalDateTimeUtil.toEpochMilli(dateTime);
    }

    /**
     * 上一个周期的日期
     */
    @Nonnull
    public static LocalDateTime prev(LocalDateTime date, DatePeriod period) {
        if (period == null) {
            return date;
        }

        return switch (period) {
            case DAY -> date.minusDays(1);
            case WEEK -> date.minusWeeks(1);
            case MONTH -> date.minusMonths(1);
            case QUARTER -> date.minusMonths(3);
            case HALF -> date.minusMonths(6);
            case YEAR -> date.minusYears(1);
        };
    }

    /**
     * 上一个周期的日期
     */
    @Nonnull
    public static LocalDate prev(LocalDate date, DatePeriod period) {
        if (period == null) {
            return date;
        }

        return switch (period) {
            case DAY -> date.minusDays(1);
            case WEEK -> date.minusWeeks(1);
            case MONTH -> date.minusMonths(1);
            case QUARTER -> date.minusMonths(3);
            case HALF -> date.minusMonths(6);
            case YEAR -> date.minusYears(1);
        };
    }

    /**
     * 下一个周期的日期
     */
    @Nonnull
    public static LocalDateTime next(LocalDateTime date, DatePeriod period) {
        if (period == null) {
            return date;
        }

        return switch (period) {
            case DAY -> date.plusDays(1);
            case WEEK -> date.plusWeeks(1);
            case MONTH -> date.plusMonths(1);
            case QUARTER -> date.plusMonths(3);
            case HALF -> date.plusMonths(6);
            case YEAR -> date.plusYears(1);
        };
    }

    /**
     * date所在月的最后一天
     */
    public static LocalDate endOfMonth(@Nonnull LocalDate date) {
        val m = date.getMonthValue();

        val ld = switch (m) {
            case 2 -> date.isLeapYear() ? 29 : 28;
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };

        return LocalDateTimeUtil.parseDate(LocalDateTimeUtil.format(date, "yyyyMM") + ld, "yyyyMMdd");
    }

    /**
     * date所在月的最后一天
     */
    public static LocalDateTime endOfMonth(@Nonnull LocalDateTime date) {
        val m = date.getMonthValue();

        val ld = switch (m) {
            case 2 -> date.toLocalDate().isLeapYear() ? 29 : 28;
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };

        return LocalDateTimeUtil.parse(LocalDateTimeUtil.format(date, "yyyyMM") + ld + " 23:59:59",
                "yyyyMMdd HH:mm:ss");
    }

}

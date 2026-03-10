/*
 * Copyright 2026 the original author or authors.
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
package com.apzda.hajimi.ucenter.service.dto;

import cn.hutool.core.date.DatePattern;
import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.kalami.data.AbstractPageQuery;
import com.apzda.kalami.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TransQueryDto extends AbstractPageQuery {

    /**
     * 资产
     */
    private Asset asset;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    @DateTimeFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @JsonFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    @DateTimeFormat(pattern = DatePattern.NORM_DATE_PATTERN)
    private LocalDate endDate;

    public Long getStartDate() {
        return DateUtils.toEpochMilli(DateUtils.beginOfDay(startDate));
    }

    public Long getEndDate() {
        return DateUtils.toEpochMilli(DateUtils.endOfDay(endDate));
    }

}

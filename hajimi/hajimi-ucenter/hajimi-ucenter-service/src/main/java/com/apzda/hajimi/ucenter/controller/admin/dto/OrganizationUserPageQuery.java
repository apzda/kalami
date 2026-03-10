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
package com.apzda.hajimi.ucenter.controller.admin.dto;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.apzda.kalami.data.AbstractPageQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class OrganizationUserPageQuery extends AbstractPageQuery {

    private String id;

    private String name;

    private String code;

    private String uid;

    private String tenantId;

    /**
     * 创建时间开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createStartTime;

    /**
     * 创建时间结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createEndTime;

    public void setCreateTimeRange(String[] createTimeRange) {
        if (createTimeRange != null && createTimeRange.length == 2) {
            try {
                this.createStartTime = LocalDateTimeUtil.parse(createTimeRange[0], "yyyy-MM-dd HH:mm:ss");
                this.createEndTime = LocalDateTimeUtil.parse(createTimeRange[1], "yyyy-MM-dd HH:mm:ss");
            }
            catch (Exception e) {
                log.warn("Cannot parse create time range: {}", e.getMessage());
            }
        }
    }

    public Long getCreateEndTime() {
        if (this.createEndTime == null) {
            return null;
        }
        return LocalDateTimeUtil.toEpochMilli(this.createEndTime);
    }

    public Long getCreateStartTime() {
        if (this.createStartTime == null) {
            return null;
        }

        return LocalDateTimeUtil.toEpochMilli(this.createStartTime);
    }

}

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
import com.apzda.kalami.utils.DateUtils;
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
@Slf4j
@Data
public class OAuthSessionPageQuery extends AbstractPageQuery {

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 认证源
     */
    private String provider;

    /**
     * 认证应用
     */
    private String appId;

    /**
     * OpenId
     */
    private String openId;

    /**
     * UnionId
     */
    private String unionId;

    /**
     * 初次登录时间开始
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime loginStartTime;

    /**
     * 初次登录时间结束
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime loginEndTime;

    /**
     * 初次登录IP
     */
    private String ip;

    /**
     * 初次登录设备
     */
    private String device;

    /**
     * 来源追踪
     */
    private String spm;

    public void setLoginRange(String[] loginRange) {
        if (loginRange != null && loginRange.length == 2) {
            try {
                this.loginStartTime = LocalDateTimeUtil.parse(loginRange[0], "yyyy-MM-dd HH:mm:ss");
                this.loginEndTime = LocalDateTimeUtil.parse(loginRange[1], "yyyy-MM-dd HH:mm:ss");
            }
            catch (Exception e) {
                log.warn("Cannot parse log time range: {}", e.getMessage());
            }
        }
    }

    public Long getLoginEndTime() {
        if (this.loginEndTime == null) {
            return null;
        }
        return LocalDateTimeUtil.toEpochMilli(this.loginEndTime);
    }

    public Long getLoginStartTime() {
        if (this.loginStartTime == null) {
            this.loginStartTime = DateUtils.beginOfDay(LocalDateTimeUtil.now().minusDays(3));
        }

        return LocalDateTimeUtil.toEpochMilli(this.loginStartTime);
    }

}

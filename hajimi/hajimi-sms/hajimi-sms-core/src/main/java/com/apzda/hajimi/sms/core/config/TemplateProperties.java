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
package com.apzda.hajimi.sms.core.config;

import lombok.Data;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Data
public class TemplateProperties {

    /**
     * 短信服务商处的ID
     */
    private String templateId;

    private String name;

    private String signName;

    /**
     * 发送间隔
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration interval = Duration.ofSeconds(60);

    /**
     * 有效期
     */
    @DurationUnit(ChronoUnit.MINUTES)
    private Duration timeout = Duration.ofMinutes(30);

    /**
     * 每分钟可以发送数量
     */
    private Integer countM;

    /**
     * 每小时可以发送数量
     */
    private Integer countH;

    /**
     * 每天可以发送数量
     */
    private Integer countD;

    /**
     * 短信正文
     */
    private String content;

    public Integer theCountM() {
        return countM == null ? 1 : countM;
    }

    public Integer theCountH() {
        return countH == null ? 10 : countH;
    }

    public Integer theCountD() {
        return countD == null ? 15 : countD;
    }

}

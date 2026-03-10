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

import com.apzda.hajimi.ucenter.domain.enums.Asset;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class TransitionDto {

    /**
     * 租户ID，默认为"0"
     */
    private String tenantId;

    /**
     * 交易ID
     */
    private Long transId;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 资产
     */
    private Asset asset;

    /**
     * 业务线
     */
    private String bizType;

    /**
     * 业务单号
     */
    private String bizOrderNo;

    /**
     * 取消原因
     */
    private String reason;

    public String getTenantId() {
        return StringUtils.defaultIfBlank(this.tenantId, "0");
    }

}

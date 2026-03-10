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
package com.apzda.hajimi.auditor.data.vo;

import com.apzda.kalami.dictionary.Dict;
import com.apzda.kalami.user.UidTransformer;
import lombok.Data;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class AuditLogVo {

    private Long id;

    private Long createdAt;

    private String createdBy;

    private Long updatedAt;

    private String updatedBy;

    private Boolean deleted;

    private String tenantId;

    private Boolean template;

    @Dict(transformer = UidTransformer.class)
    private String userId;

    private String target;

    private Long logTime;

    private String activity;

    @Dict(transformer = UidTransformer.class)
    private String runas;

    private String level;

    private String ip;

    private String device;

    private String message;

    private List<String> args;

    private String oldValue;

    private String newValue;

}

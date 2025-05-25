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
package com.apzda.kalami.data.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLog implements Serializable {

    @Serial
    private static final long serialVersionUID = -8214741335677509857L;

    private long timestamp;

    private String userId;

    private String tenantId;

    private Boolean template;

    private String activity;

    private String target;

    private String runas;

    private String level;

    private String ip;

    private String device;

    private String message;

    private List<String> args = new ArrayList<>();

    private transient Object oldValue;

    private transient Object newValue;

}

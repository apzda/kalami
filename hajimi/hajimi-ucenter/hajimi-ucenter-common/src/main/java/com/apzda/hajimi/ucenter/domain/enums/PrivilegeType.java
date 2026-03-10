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
package com.apzda.hajimi.ucenter.domain.enums;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public enum PrivilegeType {

    resource("资源权限"), authority("认证授权");

    @Getter
    @JsonPropertyDescription
    private final String description;

    PrivilegeType(String name) {
        this.description = name;
    }

}

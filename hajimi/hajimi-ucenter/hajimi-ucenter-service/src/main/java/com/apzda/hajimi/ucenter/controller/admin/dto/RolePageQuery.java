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

import com.apzda.kalami.data.AbstractPageQuery;
import com.apzda.kalami.data.domain.TenantAware;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RolePageQuery extends AbstractPageQuery implements TenantAware<String> {

    private String id;

    private String name;

    private String role;

    private String tenantId;

    /**
     * 可租赁
     */
    private Boolean leasable;

}

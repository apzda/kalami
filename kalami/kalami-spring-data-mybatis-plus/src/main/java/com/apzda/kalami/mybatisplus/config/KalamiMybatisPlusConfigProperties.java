/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.mybatisplus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Data
@ConfigurationProperties("kalami.mybatis-plus")
public class KalamiMybatisPlusConfigProperties {

    private String tenantIdColumn = "tenant_id";

    private boolean disableTenantPlugin = true;

    private final List<String> createdAtColumns = new ArrayList<>(List.of("createdAt", "createTime"));

    private final List<String> createdByColumns = new ArrayList<>(List.of("createdBy", "creatorId", "createBy"));

    private final List<String> updatedAtColumns = new ArrayList<>(List.of("updatedAt", "updateTime"));

    private final List<String> updatedByColumns = new ArrayList<>(List.of("updatedBy", "updaterId", "updateBy"));

}

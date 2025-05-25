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
package com.apzda.kalami.boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "kalami.boot.dictionary")
public class XkaBootConfigProperties {

    private boolean enabled = true;

    private String tableName = "sys_dict_item";

    private String codeColumn = "dict_code";

    private String labelColumn = "dict_label";

    private String valueColumn = "dict_value";

    private String deletedColumn;

    private String notDeletedValue;

    private String labelSuffix = "Text";

}

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
package com.apzda.hajimi.sms.core.dto;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Data
@EqualsAndHashCode(of = { "name", "value" })
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Variable {

    private String name;

    private String value;

    private int index;

    public static String toJsonStr(List<Variable> variables) {
        if (CollectionUtils.isEmpty(variables)) {
            return "";
        }
        Map<String, String> params = new HashMap<>();
        for (Variable variable : variables) {
            params.put(variable.getName(), variable.getValue());
        }
        return JSONUtil.toJsonStr(params);
    }

}

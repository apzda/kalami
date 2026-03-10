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
package com.apzda.hajimi.configure.setting;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class LabelConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 5684646475386434205L;

    /**
     * 标签: <pre>
     * KEY: # 语言
     *  KEY: TEXT # 标签与翻译
     * </pre>
     */
    private Map<String, Map<String, String>> labels;

    public Map<String, Map<String, String>> getLabels() {
        if (labels == null) {
            return new HashMap<>();
        }
        return labels;
    }

}

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

import cn.hutool.core.comparator.VersionComparator;
import com.apzda.hajimi.configure.Setting;
import lombok.Data;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class VersionSetting implements Setting {

    @Override
    public String name() {
        return "版本配置";
    }

    private String version;

    /**
     * 比较两个版本<br>
     * null版本排在最小：即： <pre>
     * compare(null, "v1") &lt; 0
     * compare("v1", "v1")  = 0
     * compare(null, null)   = 0
     * compare("v1", null) &gt; 0
     * compare("1.0.0", "1.0.2") &lt; 0
     * compare("1.0.2", "1.0.2a") &lt; 0
     * compare("1.0.3", "1.0.2a") &gt; 0
     * compare("1.13.0", "1.12.1c") &gt; 0
     * compare("V0.0.20170102", "V0.0.20170101") &gt; 0
     * </pre>
     * @param ver1 版本1
     */
    public int compare(String ver1) {
        return VersionComparator.INSTANCE.compare(this.version, ver1);
    }

}

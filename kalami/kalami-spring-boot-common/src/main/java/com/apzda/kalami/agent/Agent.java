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
package com.apzda.kalami.agent;

import com.apzda.kalami.data.ITree;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Agent implements ITree<String>, Serializable {

    @Serial
    private static final long serialVersionUID = 5675940651959721575L;

    private String id;

    private String pid;

    private String name;

    private String shortName;

    private String phone;

    private String contact;

    private String address;

    private String uid;

    private String status;

    private String remark;

    private Map<String, Double> ratios;

    private Map<String, String> merNo;

    private List<Agent> children;

    @JsonIgnore
    public Double getRatio(String type, Double defaultRatio) {
        if (ratios != null && ratios.containsKey(type)) {
            return ratios.get(type);
        }
        return defaultRatio;
    }

    @JsonIgnore
    public Double getRatio(String type) {
        return getRatio(type, 0D);
    }

    @JsonIgnore
    @Nullable
    public String getHuiFuMerNo() {
        if (CollectionUtils.isEmpty(this.merNo)) {
            return null;
        }

        return this.merNo.get("huifu");
    }

}

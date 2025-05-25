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
package com.apzda.kalami.boot.query;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Getter
public class QueryCondition implements Serializable {

    @Serial
    private static final long serialVersionUID = 4740166316629191651L;

    private String field;

    /** 组件的类型（例如：input、select、radio） */
    private String type;

    /**
     * 对应的数据库字段的类型 支持：int、bigDecimal、short、long、float、double、boolean
     */
    private String dbType;

    private String rule;

    private String val;

    public void setField(String field) {
        this.field = field;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (field == null || field.isEmpty()) {
            return "";
        }
        sb.append(this.field)
            .append(" ")
            .append(this.rule)
            .append(" ")
            .append(this.type)
            .append(" ")
            .append(this.dbType)
            .append(" ")
            .append(this.val);
        return sb.toString();
    }

}

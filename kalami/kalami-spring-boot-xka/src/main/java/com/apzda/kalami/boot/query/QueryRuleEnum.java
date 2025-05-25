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

import jakarta.annotation.Nullable;
import lombok.Getter;

import static com.apzda.kalami.utils.StringUtil.isEmpty;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Getter
public enum QueryRuleEnum {

    /** 查询规则 大于 */
    GT(">", "gt", "大于"),
    /** 查询规则 大于等于 */
    GE(">=", "ge", "大于等于"),
    /** 查询规则 小于 */
    LT("<", "lt", "小于"),
    /** 查询规则 小于等于 */
    LE("<=", "le", "小于等于"),
    /** 查询规则 等于 */
    EQ("=", "eq", "等于"),
    /** 查询规则 不等于 */
    NE("!=", "ne", "不等于"),
    /** 查询规则 包含 */
    IN("IN", "in", "包含"),
    /** 查询规则 全模糊 */
    LIKE("LIKE", "like", "全模糊"),
    /** 查询规则 左模糊 */
    LEFT_LIKE("LEFT_LIKE", "left_like", "左模糊"),
    /** 查询规则 右模糊 */
    RIGHT_LIKE("RIGHT_LIKE", "right_like", "右模糊"),
    /** 查询规则 带加号等于 */
    EQ_WITH_ADD("EQWITHADD", "eq_with_add", "带加号等于"),
    /** 查询规则 多词模糊匹配 */
    LIKE_WITH_AND("LIKEWITHAND", "like_with_and", "多词模糊匹配"),
    /** 查询规则 自定义SQL片段 */
    SQL_RULES("USE_SQL_RULES", "ext", "自定义SQL片段");

    private final String value;

    private final String condition;

    private final String msg;

    QueryRuleEnum(String value, String condition, String msg) {
        this.value = value;
        this.condition = condition;
        this.msg = msg;
    }

    @Nullable
    public static QueryRuleEnum getByValue(String value) {
        if (isEmpty(value)) {
            return null;
        }
        for (QueryRuleEnum val : values()) {
            if (val.getValue().equals(value) || val.getCondition().equals(value)) {
                return val;
            }
        }
        return null;
    }

}

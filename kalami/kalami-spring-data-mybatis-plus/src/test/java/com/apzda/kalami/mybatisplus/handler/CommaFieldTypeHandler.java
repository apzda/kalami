/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.mybatisplus.handler;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.val;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public class CommaFieldTypeHandler extends BaseTypeHandler<List<String>> {

    private static final String SEP = "<->";

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<String> strings, JdbcType jdbcType)
            throws SQLException {
        val value = Joiner.on(SEP).skipNulls().join(strings);
        preparedStatement.setString(i, value);
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        final String value = resultSet.getString(s);
        return StringUtils.isBlank(value) ? null : parse(value);
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        final String value = resultSet.getString(i);
        return StringUtils.isBlank(value) ? null : parse(value);
    }

    @Override
    public List<String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        final String value = callableStatement.getString(i);
        return StringUtils.isBlank(value) ? null : parse(value);
    }

    private List<String> parse(String value) {
        return Lists.newArrayList(Splitter.on(SEP).omitEmptyStrings().trimResults().split(value));
    }

}

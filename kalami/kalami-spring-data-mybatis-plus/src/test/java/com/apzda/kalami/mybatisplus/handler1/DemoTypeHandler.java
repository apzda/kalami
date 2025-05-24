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
package com.apzda.kalami.mybatisplus.handler1;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public class DemoTypeHandler extends BaseTypeHandler<Double> {

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Double aDouble, JdbcType jdbcType)
            throws SQLException {
        preparedStatement.setDouble(i, aDouble * 100);
    }

    @Override
    public Double getNullableResult(ResultSet resultSet, String s) throws SQLException {
        return resultSet.getDouble(s) / 100;
    }

    @Override
    public Double getNullableResult(ResultSet resultSet, int i) throws SQLException {
        return resultSet.getDouble(i) / 100;
    }

    @Override
    public Double getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        return callableStatement.getDouble(i) / 100;
    }

}

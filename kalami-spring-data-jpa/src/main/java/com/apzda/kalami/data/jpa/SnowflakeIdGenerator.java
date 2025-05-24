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
package com.apzda.kalami.data.jpa;

import cn.hutool.core.lang.Snowflake;
import com.apzda.kalami.utils.BeanUtils;
import com.apzda.kalami.utils.SnowflakeUtil;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.util.StringUtils;

import java.util.Properties;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public class SnowflakeIdGenerator implements IdentifierGenerator {

    private final static Snowflake SNOWFLAKE = SnowflakeUtil.SNOWFLAKE;

    static String TARGET_COLUMN = "target_column";

    private Type type;

    private String prefix;

    @Override
    public void configure(Type type, @Nonnull Properties parameters, ServiceRegistry serviceRegistry) {
        this.type = type;
        val entityClz = parameters.getProperty(IdentifierGenerator.ENTITY_NAME);
        val idField = parameters.getProperty(TARGET_COLUMN);
        try {
            val id = BeanUtils.getField(Class.forName(entityClz), idField);
            if (id == null) {
                throw new IllegalStateException("Could not find id field - " + idField);
            }
            val ann = id.getAnnotation(SnowflakeId.class);
            if (ann == null) {
                throw new IllegalStateException("Could not find @SnowflakeId annotation - " + idField);
            }
            prefix = ann.prefix();
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, @Nonnull Object owner) {
        if (StringUtils.hasText(prefix)) {
            return String.format("%s%d", prefix, SNOWFLAKE.nextId());
        }

        if (type.getReturnedClass().equals(String.class)) {
            return String.valueOf(SNOWFLAKE.nextId());
        }
        return SNOWFLAKE.nextId();
    }

}

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
package com.apzda.hajimi.configure;

import com.apzda.hajimi.configure.exception.SettingUnavailableException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.Data;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Revision {

    private long createdAt;

    private String settingKey;

    private Integer revision;

    private String settingCls;

    private String setting;

    @Nonnull
    public <T> T wrapped(@Nonnull Class<T> clazz, @Nonnull ObjectMapper mapper) {
        try {
            return mapper.readValue(setting, clazz);
        }
        catch (JsonProcessingException e) {
            throw new SettingUnavailableException(settingKey, e);
        }
    }

}

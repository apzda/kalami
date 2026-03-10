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
package com.apzda.hajimi.ucenter.mfa;

import com.apzda.kalami.data.MapConfig;
import jakarta.annotation.Nonnull;

import java.util.Map;

/**
 * 多因素认证器
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface Authenticator {

    @Nonnull
    String getType();

    boolean verify(String code, String secretKey);

    default String getConfig(String username, String secretKey, MapConfig<Map<String, Object>> props) {
        return null;
    }

    default String getSecretKey() {
        return null;
    }

}

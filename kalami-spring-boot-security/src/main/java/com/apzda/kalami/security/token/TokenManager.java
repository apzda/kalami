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

package com.apzda.kalami.security.token;

import jakarta.annotation.Nonnull;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
public interface TokenManager {

    /**
     * 从<code>accessToken</code>恢复认证.
     */
    Authentication restore(String accessToken);

    /**
     * 基于认证生成JwtToken
     */
    default JwtToken create(@Nonnull Authentication authentication) {
        return create(null, authentication);
    }

    /**
     * 基于原JwtToken和认证生成新的JwtToken
     */
    JwtToken create(JwtToken oldToken, @Nonnull Authentication authentication);

    /**
     * 刷新原JwtToken
     */
    JwtToken refresh(@NonNull JwtToken jwtToken, UserDetails userDetails);

    /**
     * 保证认证信息
     */
    default void save(@Nonnull Authentication authentication) {

    }

    /**
     * 删除认证信息
     */
    default void remove(@Nonnull Authentication authentication) {

    }

    /**
     * 验证认证的合法性
     */
    default void verify(@Nonnull Authentication authentication) {
    }

}

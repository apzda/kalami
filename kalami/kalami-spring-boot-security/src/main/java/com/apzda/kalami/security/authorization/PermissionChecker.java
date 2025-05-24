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

package com.apzda.kalami.security.authorization;

import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
public interface PermissionChecker extends Ordered {

    Boolean check(Authentication authentication, Object obj, String permission);

    Boolean check(Authentication authentication, Serializable targetId, String targetType, String permission);

    boolean supports(Class<?> objClazz);

    boolean supports(String targetType);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}

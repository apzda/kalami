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

package com.apzda.kalami.security.authorization.checker;

import com.apzda.kalami.security.utils.SecurityUtils;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class PermitChecker implements AuthorizationChecker {

    @Override
    public @NotNull String name() {
        return "Permit";
    }

    @Override
    public void check(@NotNull Authentication authentication, @NotNull Map<String, Object> args) {
        val arg = args.get("arg");

        if (arg instanceof String perm && !SecurityUtils.hasPermission(perm)) {
            throw new AccessDeniedException("error.403");
        }

        val authority = args.get("authority");
        if (authority instanceof String auth && !SecurityUtils.hasAuthority(auth)) {
            throw new AccessDeniedException("error.403");
        }

        val permission = args.get("permission");
        if (permission instanceof String perm && !SecurityUtils.hasPermission(perm)) {
            throw new AccessDeniedException("error.403");
        }
    }

}

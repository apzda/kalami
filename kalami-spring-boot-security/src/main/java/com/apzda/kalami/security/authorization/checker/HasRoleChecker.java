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
import jakarta.annotation.Nonnull;
import lombok.val;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/22
 * @version 1.0.0
 */
public class HasRoleChecker implements AuthorizationChecker {

    @Nonnull
    @Override
    public String name() {
        return "HasRole";
    }

    @Override
    public void check(@Nonnull Authentication authentication, @Nonnull Map<String, Object> args) {
        Object roles = args.get("roles");
        val any = Objects.requireNonNullElse(args.get("any"), "true").toString();
        String[] rList;

        if (roles instanceof Collection<?> roleCollection) {
            rList = (String[]) roleCollection.stream().map(Object::toString).toArray();
        }
        else if (roles instanceof String[]) {
            rList = (String[]) roles;
        }
        else if (roles instanceof String) {
            rList = new String[] { (String) roles };
        }
        else {
            return;
        }

        if (("true".equalsIgnoreCase(any) && !SecurityUtils.hasAnyRole(rList)) || !SecurityUtils.hasRole(rList)) {
            throw new AccessDeniedException("error.403");
        }
    }

}

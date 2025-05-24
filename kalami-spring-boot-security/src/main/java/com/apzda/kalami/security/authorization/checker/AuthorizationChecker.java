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

import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.user.CurrentUserProvider;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;

import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/22
 * @version 1.0.0
 */
public interface AuthorizationChecker {

    Logger log = LoggerFactory.getLogger(AuthorizationChecker.class);

    PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Nonnull
    String name();

    void check(@Nonnull Authentication authentication, @Nonnull Map<String, Object> args);

    static boolean check(String url, SecurityConfigProperties properties, Map<String, AuthorizationChecker> checkers) {
        val currentUser = CurrentUserProvider.getCurrentUser();
        if (currentUser.isAuthenticated()) {
            val authentication = SecurityContextHolder.getContext().getAuthentication();
            for (SecurityConfigProperties.Checker config : properties.getChecker()) {
                val path = config.getPath();
                val checkersConfig = config.getCheckers();
                if (StringUtils.isBlank(path) || CollectionUtils.isEmpty(checkersConfig)) {
                    log.warn("Invalid filter config, path or checkers is empty: {}", path);
                    continue;
                }
                if (!PATH_MATCHER.match(path, url)) {
                    log.trace("Bypass '{}' check since it does not match '{}'", url, path);
                    continue;
                }

                for (val checker : checkersConfig) {
                    if (!checker.isEnabled()) {
                        continue;
                    }
                    val name = checker.getName();
                    val checkerBean = checkers.get(name);
                    if (checkerBean == null) {
                        log.warn("AuthenticationChecker '{}' not found for '{}'", name, url);
                        continue;
                    }
                    checkerBean.check(authentication, checker.getArgs());
                    log.trace("Bypass '{}' check({})", url, name);
                }
            }

            return true;
        }

        return false;
    }

}

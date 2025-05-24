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

package com.apzda.kalami.security.context;

import com.apzda.kalami.security.authentication.AuthenticationDetails;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.user.CurrentUser;
import com.apzda.kalami.user.CurrentUserProvider;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Slf4j
public class SpringSecurityUserProvider extends CurrentUserProvider {

    @Override
    protected CurrentUser currentUser() {
        val context = SecurityContextHolder.getContext();
        if (context == null) {
            log.trace("Current user: context is null");
            return null;
        }

        val authentication = context.getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.trace("Current user: authentication is null or not authenticated");
            return null;
        }

        val builder = getCurrentUserBuilder(authentication);

        return builder.build();
    }

    @Nonnull
    public static CurrentUser.CurrentUserBuilder getCurrentUserBuilder(@Nonnull Authentication authentication) {
        val builder = CurrentUser.builder();
        val uid = authentication.getName();
        val details = authentication.getDetails();

        builder.uid(uid);

        if (details instanceof AuthenticationDetails device) {
            builder.userAgent(device.getUserAgent());
            builder.app(device.getApp());
            builder.device(device.getDevice());
            builder.remoteAddress(device.getRemoteAddress());
            builder.meta(device.getMeta());
        }

        if (authentication instanceof JwtTokenAuthentication token) {
            val jwtToken = token.getJwtToken();

            if (jwtToken != null) {
                builder.runAs(jwtToken.getRunAs());
            }
        }

        builder.authenticated(authentication.isAuthenticated());

        return builder;
    }

}

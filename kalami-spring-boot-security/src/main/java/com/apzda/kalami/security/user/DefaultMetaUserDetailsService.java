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

package com.apzda.kalami.security.user;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/21
 * @version 1.0.0
 */
@Slf4j
public class DefaultMetaUserDetailsService implements MetaUserDetailsService {

    public DefaultMetaUserDetailsService() {

    }

    @Nonnull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(@Nonnull UserDetails userDetails,
            Authentication authentication) {
        // 不知道从哪儿加载
        log.trace("[3] Retrieved authorities: []");
        return List.of();
    }

}

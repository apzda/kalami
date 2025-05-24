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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.style.ToStringCreator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/21
 * @version 1.0.0
 */
@Slf4j
@Data
public class DefaultMetaUserDetails implements MetaUserDetails {

    private final UserDetails userDetails;

    private final MetaUserDetailsService userMetaService;

    private Authentication authentication;

    private Collection<? extends GrantedAuthority> authorities;

    public DefaultMetaUserDetails(UserDetails userDetails, MetaUserDetailsService userMetaService) {
        Assert.notNull(userDetails, "userDetails must not be null");
        Assert.notNull(userMetaService, "userMetaService must not be null");
        this.userDetails = userDetails;
        this.userMetaService = userMetaService;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.trace("[2] Retrieving authorities: {}", userDetails);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        if (!CollectionUtils.isEmpty(authorities)) {
            log.trace("[3] Retrieved authorities from 'userDetails': {}", authorities);
            return authorities;
        }

        authorities = userMetaService.getAuthorities(userDetails, authentication);
        log.trace("[z] Retrieved authorities from 'MetaUserDetailsService': {}", authorities);

        return authorities;
    }

    @Override
    public String getPassword() {
        return userDetails.getPassword();
    }

    @Override
    public String getUsername() {
        return userDetails.getUsername();
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("userDetails", userDetails.getUsername()).toString();
    }

}

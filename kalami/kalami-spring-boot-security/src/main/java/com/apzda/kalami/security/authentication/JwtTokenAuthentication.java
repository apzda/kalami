/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.security.authentication;

import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.security.token.JwtToken;
import com.apzda.kalami.security.user.MetaUserDetails;
import com.apzda.kalami.security.utils.SecurityUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Slf4j
public class JwtTokenAuthentication extends AbstractAuthenticationToken {

    @Getter
    protected JwtToken jwtToken;

    private final UserDetails principal;

    private transient Object credentials;

    private Collection<GrantedAuthority> authorities;

    JwtTokenAuthentication(UserDetails principal, Object credentials) {
        super(null);
        this.principal = SecurityUtils.checkUserDetails(principal);
        this.credentials = credentials;
        setAuthenticated(false);
        if (principal instanceof MetaUserDetails meta) {
            meta.setAuthentication(this);
        }
    }

    JwtTokenAuthentication(UserDetails principal, Object credentials,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = SecurityUtils.checkUserDetails(principal);
        this.credentials = credentials;

        if (principal instanceof MetaUserDetails meta) {
            meta.setAuthentication(this);
        }
        super.setAuthenticated(true); // must use super, as we override

        if (this.getDetails() == null) {
            KalamiContextHolder.getRequest().ifPresent(request -> {
                this.setDetails(new DeviceAuthenticationDetailsSource().buildDetails(request));
            });
        }
    }

    JwtTokenAuthentication(UserDetails principal, Object credentials, AuthenticationDetails details) {
        super(List.of());
        this.principal = SecurityUtils.checkUserDetails(principal);
        this.credentials = credentials;

        if (principal instanceof MetaUserDetails meta) {
            meta.setAuthentication(this);
        }
        super.setAuthenticated(true); // must use super, as we override
        this.setDetails(details);
    }

    @Serial
    private void readObject(@Nonnull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.credentials = null;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Nonnull
    public UserDetails getUserDetails() {
        return this.principal;
    }

    @Nullable
    public AuthenticationDetails getAuthDetails() {
        if (getDetails() instanceof AuthenticationDetails authDetails) {
            return authDetails;
        }
        return null;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated,
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        // super.eraseCredentials();
        this.credentials = null;
    }

    @Nonnull
    public static JwtTokenAuthentication unauthenticated(UserDetails principal, Object credentials) {
        return new JwtTokenAuthentication(principal, credentials);
    }

    @Nonnull
    public static JwtTokenAuthentication authenticated(UserDetails principal, Object credentials) {
        return new JwtTokenAuthentication(principal, credentials, Collections.emptyList());
    }

    @Nonnull
    public static JwtTokenAuthentication authenticated(UserDetails principal, Object credentials,
            AuthenticationDetails details) {
        return new JwtTokenAuthentication(principal, credentials, details);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> getAuthorities() {
        if (this.authorities != null) {
            return this.authorities;
        }

        synchronized (principal) {
            if (this.authorities != null) {
                return this.authorities;
            }
            if (log.isTraceEnabled()) {
                log.trace("[1] Retrieving authorities: {}", getName());
            }
            val authorities = principal.getAuthorities();
            if (!CollectionUtils.isEmpty(authorities)) {
                this.authorities = (Collection<GrantedAuthority>) authorities;
            }
            else {
                this.authorities = Collections.emptyList();
            }
            return this.authorities;
        }
    }

    public void setJwtToken(@Nonnull JwtToken jwtToken) {
        Assert.notNull(jwtToken, "JwtToken must not be null");
        Assert.hasText(jwtToken.getAccessToken(), "JwtToken must not be null");

        this.jwtToken = jwtToken;
        super.setAuthenticated(true);
    }

}

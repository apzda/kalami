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

package com.apzda.kalami.security.authentication.filter;

import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.exception.BizException;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Slf4j
@Getter
@Deprecated
@SuppressWarnings("all")
public abstract class AbstractAuthenticatedFilter extends OncePerRequestFilter implements Ordered {

    public static final String IGNORED = "GSVC.FILTER.IGNORED";

    protected final Set<RequestMatcher> excludes;

    protected final SecurityConfigProperties properties;

    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy();

    public AbstractAuthenticatedFilter(SecurityConfigProperties properties) {
        this(Collections.emptySet(), properties);
    }

    public AbstractAuthenticatedFilter(@Nonnull Set<RequestMatcher> excludes,
            @Nonnull SecurityConfigProperties properties) {
        this.properties = properties;
        this.excludes = excludes;
    }

    public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy strategy) {
        Assert.notNull(strategy, "securityContextHolderStrategy cannot be null");
        securityContextHolderStrategy = strategy;
    }

    @Nullable
    protected Authentication getAuthentication() {
        return securityContextHolderStrategy.getContext().getAuthentication();
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain) throws ServletException, IOException {

        if (request.getAttribute(IGNORED) != null) {
            filterChain.doFilter(request, response);
            return;
        }

        for (RequestMatcher exclude : excludes) {
            if (exclude.matches(request)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        val authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails)) {
            request.setAttribute(IGNORED, Boolean.TRUE);
            filterChain.doFilter(request, response);
            return;
        }

        if (doFilter(authentication, (UserDetails) authentication.getPrincipal())) {
            filterChain.doFilter(request, response);
        }
        else {
            throw new BizException(ServiceError.FORBIDDEN);
        }
    }

    protected abstract boolean doFilter(@Nonnull Authentication authentication, @Nonnull UserDetails userDetails);

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}

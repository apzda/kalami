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

package com.apzda.kalami.security.web.interceptor;

import com.apzda.kalami.security.authorization.checker.AuthorizationChecker;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.utils.SecurityUtils;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StopWatch;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/19
 * @version 1.0.0
 */
@Slf4j
public class KalamiServletSecurityInterceptor implements HandlerInterceptor {

    private static final PathMatcher PATH_MATCHER = AuthorizationChecker.PATH_MATCHER;

    private final SecurityConfigProperties properties;

    private final Map<String, AuthorizationChecker> checkerBeans = new HashMap<>();

    ;

    public KalamiServletSecurityInterceptor(@Nonnull SecurityConfigProperties properties,
            ObjectProvider<AuthorizationChecker> filtersProvider) {
        this.properties = properties;
        this.initialize(filtersProvider);
    }

    private void initialize(@Nonnull ObjectProvider<AuthorizationChecker> filtersProvider) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Initializing HandlerInterceptor 'KalamiServletSecurityInterceptor'");
        filtersProvider.orderedStream().forEach(filter -> {
            val name = filter.name();
            if (StringUtils.isNotBlank(name)) {
                checkerBeans.put(name, filter);
                log.debug("AuthorizationChecker '{}' configured for use", name);
            }
            else {
                log.warn("The name of Filter '{}' is not defined", filter.getClass().getName());
            }
        });
        stopWatch.stop();
        log.info("KalamiServletSecurityInterceptor: initialization completed in {} ms", stopWatch.getTotalTimeMillis());
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
            @Nonnull Object handler) throws Exception {
        val excludes = properties.getExcludes();
        val requestURI = request.getRequestURI();
        if (!CollectionUtils.isEmpty(excludes)) {
            for (val exclude : excludes) {
                if (PATH_MATCHER.match(exclude, requestURI)) {
                    log.trace("Bypass '{}' since it is excluded by '{}'", requestURI, exclude);
                    return true;
                }
            }
        }

        val context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null && context.getAuthentication().isAuthenticated()
                && !CollectionUtils.isEmpty(checkerBeans)
                && AuthorizationChecker.check(requestURI, properties, checkerBeans)) {
            return true;
        }

        val exception = request.getAttribute(SecurityUtils.CONTEXT_ATTR_EXCEPTION);
        if (exception != null) {
            log.trace("Found exception '{}' in request", exception.getClass().getName());
            throw (Exception) exception;
        }

        throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
            @Nonnull Object handler, Exception ex) throws Exception {
        SecurityUtils.DefaultSecurityExpressionHandler.ROOT_BOX.remove();
    }

}

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

package com.apzda.kalami.security.web.filter;

import com.apzda.kalami.error.ServiceError;
import com.apzda.kalami.security.authorization.checker.AuthorizationChecker;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.error.AuthenticationError;
import com.apzda.kalami.security.utils.SecurityUtils;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StopWatch;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
@Slf4j
public class KalamiWebFluxSecurityFilter implements WebFilter {

    private static final PathMatcher PATH_MATCHER = AuthorizationChecker.PATH_MATCHER;

    private final SecurityConfigProperties properties;

    private final Map<String, AuthorizationChecker> checkerBeans = new HashMap<>();

    public KalamiWebFluxSecurityFilter(@Nonnull SecurityConfigProperties properties,
            ObjectProvider<AuthorizationChecker> filtersProvider) {
        this.properties = properties;
        this.initialize(filtersProvider);
    }

    private void initialize(@Nonnull ObjectProvider<AuthorizationChecker> filtersProvider) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.debug("Initializing WebFilter 'KalamiWebFluxSecurityFilter'");
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
        log.info("KalamiWebFluxSecurityFilter: initialization completed in {} ms", stopWatch.getTotalTimeMillis());
    }

    @Override
    @Nonnull
    public Mono<Void> filter(@Nonnull ServerWebExchange exchange, @Nonnull WebFilterChain chain) {
        log.trace("KalamiWebFluxSecurityFilter filter started");
        val request = exchange.getRequest();
        val excludes = properties.getExcludes();
        val requestURI = request.getURI().getPath();

        if (!CollectionUtils.isEmpty(excludes)) {
            for (val exclude : excludes) {
                if (PATH_MATCHER.match(exclude, requestURI)) {
                    log.trace("Bypass '{}' since it is excluded by '{}'", requestURI, exclude);
                    return chain.filter(exchange);
                }
            }
        }

        val context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null && context.getAuthentication().isAuthenticated()
                && !CollectionUtils.isEmpty(checkerBeans)
                && AuthorizationChecker.check(requestURI, properties, checkerBeans)) {
            return chain.filter(exchange);
        }

        val exception = request.getAttributes().get(SecurityUtils.CONTEXT_ATTR_EXCEPTION);
        if (exception != null) {
            log.trace("Found exception '{}' in request", exception.getClass().getName());
            return Mono.error((Exception) exception);
        }

        return Mono.error(new AuthenticationError(ServiceError.UNAUTHORIZED));
    }

}

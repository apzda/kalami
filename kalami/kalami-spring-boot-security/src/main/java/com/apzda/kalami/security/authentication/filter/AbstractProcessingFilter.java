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

import com.apzda.kalami.security.authentication.DeviceAuthenticationDetailsSource;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Slf4j
@Deprecated
@SuppressWarnings("all")
public abstract class AbstractProcessingFilter extends AbstractAuthenticationProcessingFilter
        implements Ordered, ApplicationContextAware {

    protected AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new DeviceAuthenticationDetailsSource();

    protected ApplicationContext applicationContext;

    protected ObjectMapper objectMapper;

    protected AbstractProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl, null);
    }

    protected AbstractProcessingFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher, null);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.objectMapper = applicationContext.getBean(ObjectMapper.class);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    protected void setDetails(HttpServletRequest request, @Nonnull AbstractAuthenticationToken authRequest) {
        if (authRequest.getDetails() == null) {
            authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        }
    }

    @NonNull
    protected <R> R readRequestBody(HttpServletRequest request, Class<R> rClass) throws IOException {
        val req = new ContentCachingRequestWrapper(request);
        try (val reader = req.getReader()) {
            return objectMapper.readValue(reader, rClass);
        }
    }

    @NonNull
    protected <R> R readRequestBody(HttpServletRequest request, TypeReference<R> rClass) throws IOException {
        val req = new ContentCachingRequestWrapper(request);
        try (val reader = req.getReader()) {
            return objectMapper.readValue(reader, rClass);
        }
    }

}

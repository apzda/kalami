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

import com.apzda.kalami.exception.BizException;
import com.apzda.kalami.security.authentication.handler.AuthenticationHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@RequiredArgsConstructor
public class AuthenticationExceptionFilter extends OncePerRequestFilter {

    private final AuthenticationHandler authenticationHandler;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        }
        catch (AuthenticationException | BizException ae) {
            authenticationHandler.handleAuthenticationException(request, response, ae);
        }
    }

}

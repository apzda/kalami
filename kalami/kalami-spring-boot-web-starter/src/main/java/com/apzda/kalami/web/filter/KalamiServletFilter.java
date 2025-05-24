/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.web.filter;

import cn.hutool.core.lang.UUID;
import com.apzda.kalami.i18n.I18n;
import com.apzda.kalami.web.context.KalamiContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class KalamiServletFilter extends OncePerRequestFilter {

    private final LocaleResolver localeResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        val context = KalamiContextHolder.getContext();

        val requestId = StringUtils.defaultIfBlank(request.getHeader("X-Request-ID"),
                StringUtils.defaultIfBlank(MDC.get("traceId"), UUID.randomUUID().toString(true)));

        request.setAttribute("X-Request-ID", requestId);
        response.setHeader("X-Request-ID", requestId);

        context.setRequestId(requestId);
        try {
            I18n.setLocale(localeResolver.resolveLocale(request));
        }
        catch (Exception e) {
            log.debug("Cannot resolve locale - {}", e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        }
        finally {
            KalamiContextHolder.clear();
            I18n.removeLocale();
        }
    }

}

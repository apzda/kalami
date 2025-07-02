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
package com.apzda.kalami.web.i18n;

import com.apzda.kalami.context.KalamiContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.apache.commons.lang3.LocaleUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/15
 * @version 1.0.0
 */
public class LocaleResolverImpl extends CookieLocaleResolver {

    private final Locale defaultLocale;

    private final String language;

    private final AcceptHeaderLocaleResolver acceptHeaderLocaleResolver;

    public LocaleResolverImpl(@NonNull String defaultLang, @NonNull Locale defaultLocale) {
        super(defaultLang);
        setDefaultLocale(defaultLocale);
        acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
        acceptHeaderLocaleResolver.setDefaultLocale(defaultLocale);
        this.defaultLocale = defaultLocale;
        this.language = defaultLang;
    }

    @Override
    @NonNull
    public Locale resolveLocale(@Nullable HttpServletRequest request) {
        if (request == null) {
            return defaultLocale;
        }

        val cookie = KalamiContextHolder.cookie(language);

        if (cookie != null) {
            try {
                return LocaleUtils.toLocale(cookie);
            }
            catch (Exception ignored) {
            }
        }

        if (request.getHeader("Accept-Language") != null) {
            return acceptHeaderLocaleResolver.resolveLocale(request);
        }

        return super.resolveLocale(request);
    }

}

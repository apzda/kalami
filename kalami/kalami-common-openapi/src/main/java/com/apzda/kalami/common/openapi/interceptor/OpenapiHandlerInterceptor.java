/*
 * Copyright 2026 the original author or authors.
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
package com.apzda.kalami.common.openapi.interceptor;

import com.apzda.kalami.common.openapi.ClientInfoProvider;
import com.apzda.kalami.common.openapi.RequestHolder;
import com.apzda.kalami.common.openapi.annotation.OpenApi;
import com.apzda.kalami.common.openapi.config.OpenApiProperties;
import com.apzda.kalami.common.openapi.exception.OpenApiException;
import com.apzda.kalami.common.openapi.token.TokenManager;
import com.apzda.kalami.common.openapi.utils.IpUtil;
import com.apzda.kalami.i18n.I18n;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OpenapiHandlerInterceptor implements HandlerInterceptor {

    private final ClientInfoProvider provider;

    private final TokenManager tokenManager;

    private final OpenApiProperties properties;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
            @Nonnull Object handler) throws Exception {
        if (!properties.isSimpleMode()) {
            return true;
        }

        if (handler instanceof HandlerMethod hm) {
            OpenApi annotation = null;

            val method = hm.getMethod();
            if (method.isAnnotationPresent(OpenApi.class)) {
                annotation = method.getAnnotation(OpenApi.class);
            }
            else {
                val controllerClz = hm.getBeanType();
                if (controllerClz.isAnnotationPresent(OpenApi.class)) {
                    annotation = controllerClz.getAnnotation(OpenApi.class);
                }
            }

            if (annotation != null) {
                if (RequestHolder.getHead().getLanguage() == null) {
                    Locale locale = LocaleUtils.toLocale("en-US");
                    val languages = request.getHeader("Accept-Language");
                    try {
                        val acceptLanguageAsLocales = getAcceptLanguageAsLocales(languages);
                        if (!CollectionUtils.isEmpty(acceptLanguageAsLocales)) {
                            locale = acceptLanguageAsLocales.get(0);
                        }
                    }
                    catch (Exception e) {
                        log.warn("OPENAPI： 非法的Accept-Language: {}", languages);
                    }
                    RequestHolder.getHead().setLocale(locale);
                }

                val value = annotation.value();
                if (value) {
                    val authorization = request.getHeader("Authorization");
                    if (StringUtils.isBlank(authorization)) {
                        throw new OpenApiException("401", I18n.t("api.error.missingToken"));
                    }

                    if (!tokenManager.isValid(authorization)) {
                        throw new OpenApiException("401", I18n.t("api.error.tokenIsInvalid"));
                    }

                    val clientId = tokenManager.getClientId(authorization);
                    if (StringUtils.isBlank(clientId)) {
                        throw new OpenApiException("401", I18n.t("api.error.tokenIsInvalid"));
                    }

                    RequestHolder.getHead().setClientId(clientId);

                    val clientInfo = provider.getClientInfo(clientId);
                    if (clientInfo.isDisabled()) {
                        throw new OpenApiException("401", I18n.t("api.error.clientIsDisabled"));
                    }

                    val ipBlacklist = clientInfo.getIpBlacklist();
                    if (StringUtils.isNotBlank(ipBlacklist) && IpUtil.match(ipBlacklist, true)) {
                        throw new OpenApiException("403", I18n.t("api.error.ipBlacklistMatch"));
                    }

                    val ipWhitelist = clientInfo.getIpWhitelist();
                    if (StringUtils.isNotBlank(ipWhitelist) && !IpUtil.match(ipWhitelist, true)) {
                        throw new OpenApiException("403", I18n.t("api.error.ipWhitelistMatch"));
                    }
                }
            }

        }

        return true;
    }

    private List<Locale> getAcceptLanguageAsLocales(String language) {
        List<Locale.LanguageRange> ranges = getAcceptLanguage(language);
        if (ranges.isEmpty()) {
            return Collections.emptyList();
        }

        List<Locale> locales = new ArrayList<>(ranges.size());
        for (Locale.LanguageRange range : ranges) {
            if (!range.getRange().startsWith("*")) {
                locales.add(Locale.forLanguageTag(range.getRange()));
            }
        }
        return locales;
    }

    private List<Locale.LanguageRange> getAcceptLanguage(String language) {
        String value = language;
        if (org.springframework.util.StringUtils.hasText(value)) {
            try {
                return Locale.LanguageRange.parse(value);
            }
            catch (IllegalArgumentException ignored) {
                String[] tokens = org.springframework.util.StringUtils.tokenizeToStringArray(value, ",");
                for (int i = 0; i < tokens.length; i++) {
                    tokens[i] = org.springframework.util.StringUtils.trimTrailingCharacter(tokens[i], ';');
                }
                value = org.springframework.util.StringUtils.arrayToCommaDelimitedString(tokens);
                return Locale.LanguageRange.parse(value);
            }
        }
        return Collections.emptyList();
    }

}

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
package com.apzda.kalami.i18n;

import cn.hutool.core.util.ServiceLoaderUtil;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;
import java.util.Optional;

/**
 * @author fengz (windywany@gmail.com)
 * @since 2025/05/15
 * @version 1.0.0
 */
@Slf4j
public abstract class I18n implements InitializingBean {

    public static final ResourceBundleMessageSource messageSource = InternalResourceBundleMessageSource.messageSource;

    private static final ThreadLocal<Locale> localeBox = new ThreadLocal<>();

    private static Locale defaultLocale = detectDefaultLocale();

    public static String t(@Nonnull String code, Object[] args, String defaultStr, Locale locale) {
        //@formatter:off
        val codeId = code.toLowerCase()
            .replace("{}", "0")
            .replaceAll("\\W+", ".");
        //@formatter:on
        try {
            if (locale == null) {
                locale = Optional.ofNullable(localeBox.get()).orElse(defaultLocale);
            }

            return messageSource.getMessage(codeId, args, defaultStr, locale);
        }
        catch (Exception e) {
            log.warn("Cannot translate '{}' with args: {} - {}", codeId, args, e.getMessage());
            return defaultStr;
        }
    }

    public static String t(String code, String defaultStr, Locale locale) {
        return t(code, null, defaultStr, locale);
    }

    public static String t(String code, String defaultStr) {
        return t(code, null, defaultStr, null);
    }

    public static String t(@Nonnull String code) {
        val defaultStr = code.replace("_", " ");
        return t(code, null, defaultStr, null);
    }

    public static String t(@Nonnull String code, Object[] args) {
        val defaultStr = MessageFormatter.arrayFormat(code.replace("_", " "), args).getMessage();
        return t(code, args, defaultStr);
    }

    public static String t(@Nonnull String code, Object[] args, Locale locale) {
        val defaultStr = MessageFormatter.arrayFormat(code.replace("_", " "), args).getMessage();
        return t(code, args, defaultStr, locale);
    }

    public static String t(String code, Object[] args, String defaultStr) {
        return t(code, args, defaultStr, null);
    }

    public static String t(@Nonnull MessageSourceResolvable resolvable) {
        return t(resolvable, null);
    }

    public static String t(@Nonnull MessageSourceResolvable resolvable, Locale locale) {
        if (locale == null) {
            locale = Optional.ofNullable(localeBox.get()).orElse(defaultLocale);
        }
        try {
            return messageSource.getMessage(resolvable, locale);
        }
        catch (Exception e) {
            log.warn("Cannot translate '{}' - {}", resolvable, e.getMessage());
            return resolvable.getDefaultMessage();
        }
    }

    public static void addBaseNames(String... baseName) {
        messageSource.addBasenames(baseName);
    }

    public static void setParentMessageSource(@Nonnull MessageSource parentMessageSource) {
        messageSource.setParentMessageSource(parentMessageSource);
    }

    public static void setDefaultLocale(@Nonnull Locale defaultLocale) {
        I18n.defaultLocale = defaultLocale;
    }

    public static void setLocale(@Nonnull Locale locale) {
        I18n.localeBox.set(locale);
    }

    public static void removeLocale() {
        I18n.localeBox.remove();
    }

    public static Locale detectDefaultLocale() {
        val defaultLocal = System.getProperty("app.locale", System.getenv("APP_LOCALE"));

        if (StringUtils.isNotBlank(defaultLocal)) {
            try {
                val locale = LocaleUtils.toLocale(defaultLocal);
                log.info("Using '{}' as default locale", locale);
                return locale;
            }
            catch (Exception e) {
                log.warn("Cannot convert '{}' to a locale - {}", defaultLocal, e.getMessage());
            }
        }

        return Locale.getDefault();
    }

    static final class InternalResourceBundleMessageSource {

        static final ResourceBundleMessageSource messageSource = createMessageSource();

        @Nonnull
        private static ResourceBundleMessageSource createMessageSource() {
            val messageSource = new ResourceBundleMessageSource();
            messageSource.setDefaultEncoding("UTF-8");
            messageSource.setUseCodeAsDefaultMessage(true);
            messageSource.setFallbackToSystemLocale(true);
            messageSource.setCacheSeconds(60);
            messageSource.setAlwaysUseMessageFormat(true);

            val baseNameResolvers = ServiceLoaderUtil.loadList(BaseNameResolver.class);

            if (baseNameResolvers != null && !baseNameResolvers.isEmpty()) {
                for (BaseNameResolver baseNameResolver : baseNameResolvers) {
                    val baseNames = baseNameResolver.getBaseNames();
                    if (baseNames != null && !baseNames.isEmpty()) {
                        messageSource.addBasenames(baseNames.toArray(new String[0]));
                    }
                }
            }

            log.info("MessageSource BaseNames: {}", messageSource.getBasenameSet());

            return messageSource;
        }

    }

}

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
package com.apzda.kalami.context;

import com.google.common.base.Splitter;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/15
 * @version 1.0.0
 */
@Slf4j
public abstract class KalamiContextHolder implements ApplicationContextAware {

    private static final String FILTERED_HTTP_HEADERS = "FILTERED_HTTP_HEADERS";

    private static final String HTTP_COOKIES = "FILTERED_HTTP_COOKIES";

    private static final String X_FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";

    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private static final String X_FORWARDED_HOST_HEADER = "X-Forwarded-Host";

    private static final ThreadLocal<Context> CONTEXT_BOX = new ThreadLocal<>();

    private static final HttpHeaders defaultHeaders = new HttpHeaders();

    private static String appName;

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        KalamiContextHolder.appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        KalamiContextHolder.applicationContext = applicationContext;
    }

    public static Optional<HttpServletRequest> getRequest() {
        val requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes request) {
            return Optional.of(request.getRequest());
        }
        return Optional.empty();
    }

    public static Optional<HttpServletResponse> getResponse() {
        val requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes request) {
            return Optional.ofNullable(request.getResponse());
        }
        return Optional.empty();
    }

    @Nonnull
    public static HttpHeaders headers() {
        val context = getContext();
        val headers = context.headers;
        if (headers != null) {
            return headers;
        }

        val request = getRequest();
        if (request.isPresent()) {
            val httpServletRequest = request.get();
            Object filtered = httpServletRequest.getAttribute(FILTERED_HTTP_HEADERS);
            if (filtered == null) {
                synchronized (httpServletRequest) {
                    filtered = httpServletRequest.getAttribute(FILTERED_HTTP_HEADERS);
                    if (filtered == null) {
                        val httpHeaders = HttpHeaders.readOnlyHttpHeaders(createDefaultHttpHeaders(httpServletRequest));
                        val defaultHttpHeaders = new HttpHeaders();
                        defaultHttpHeaders.putAll(httpHeaders);
                        httpServletRequest.setAttribute(FILTERED_HTTP_HEADERS, defaultHttpHeaders);
                        filtered = defaultHttpHeaders;
                    }
                }
            }

            if (filtered instanceof HttpHeaders defaultHttpHeaders) {
                context.headers = defaultHttpHeaders;
                return defaultHttpHeaders;
            }
        }

        context.headers = defaultHeaders;
        return defaultHeaders;
    }

    public static String header(String name) {
        return headers().getFirst(name);
    }

    public static String header(String name, String defaultValue) {
        return org.apache.commons.lang3.StringUtils.defaultIfBlank(header(name), defaultValue);
    }

    @NonNull
    public static Map<String, String> headers(String prefix) {
        val headers = new HashMap<String, String>();
        val filtered = headers();

        filtered.keySet().forEach(header -> {
            if (StringUtils.startsWithIgnoreCase(header, prefix)) {
                headers.put(header, filtered.getFirst(header));
            }
        });

        return headers;
    }

    @Nullable
    public static String cookie(String name) {
        val httpCookie = cookies(name).get(name);
        if (httpCookie != null) {
            return httpCookie.getValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Map<String, HttpCookie> cookies() {
        val context = getContext();
        val cachedCookie = context.cookies;
        if (cachedCookie != null) {
            return cachedCookie;
        }
        val request = getRequest();
        if (request.isPresent()) {
            final HttpServletRequest httpServletRequest = request.get();
            Object filtered = httpServletRequest.getAttribute(HTTP_COOKIES);
            if (filtered != null) {
                return (Map<String, HttpCookie>) filtered;
            }
            synchronized (httpServletRequest) {
                filtered = httpServletRequest.getAttribute(HTTP_COOKIES);
                if (filtered != null) {
                    return (Map<String, HttpCookie>) filtered;
                }
                val cookies = new HashMap<String, HttpCookie>();
                val cookies1 = httpServletRequest.getCookies();
                if (cookies1 != null) {
                    for (Cookie cookie : cookies1) {
                        cookies.put(cookie.getName(), new HttpCookie(cookie.getName(), cookie.getValue()));
                    }
                }
                httpServletRequest.setAttribute(HTTP_COOKIES, cookies);
                context.cookies = cookies;
                return cookies;
            }
        }

        return Collections.emptyMap();
    }

    @Nonnull
    public static Map<String, HttpCookie> cookies(String prefix) {
        val cookies = new HashMap<String, HttpCookie>();
        cookies().forEach(((s, httpCookie) -> {
            if (StringUtils.startsWithIgnoreCase(s, prefix)) {
                cookies.put(s, httpCookie);
            }
        }));
        return cookies;
    }

    @NonNull
    public static Context getContext() {
        var context = CONTEXT_BOX.get();
        if (context == null) {
            context = new Context("", null);
            CONTEXT_BOX.set(context);
        }
        return context;
    }

    public static String getAppName() {
        return org.apache.commons.lang3.StringUtils.defaultIfBlank(appName,
                System.getProperty("spring.application.name", System.getenv("SPRING_APPLICATION_NAME")));
    }

    @Nullable
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Nonnull
    public static <T> T getBean(Class<T> beanClass) {
        if (applicationContext != null) {
            return applicationContext.getBean(beanClass);
        }

        throw new NullPointerException("applicationContext is null");
    }

    public static void restore(@NonNull Context context) {
        CONTEXT_BOX.set(context);
        MDC.put("tid", context.requestId);
    }

    public static void clear() {
        CONTEXT_BOX.remove();
    }

    public static String getRequestId() {
        val requestId = header("x-request-id");
        if (StringUtils.hasText(requestId)) {
            return requestId;
        }
        val context = CONTEXT_BOX.get();
        if (context != null) {
            return org.apache.commons.lang3.StringUtils.defaultIfBlank(context.requestId, "");
        }
        else {
            return "";
        }
    }

    @Nullable
    public static String getRemoteIp() {
        val context = getContext();
        if (StringUtils.hasText(context.remoteIp)) {
            return context.remoteIp;
        }
        val request = getRequest();
        if (request.isEmpty()) {
            return null;
        }

        context.remoteIp = getRemoteAddr();
        val headers = headers();
        val xForwardedFor = headers.getFirst(X_FORWARDED_FOR_HEADER);

        if (StringUtils.hasText(xForwardedFor)) {
            val ips = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(xForwardedFor);
            if (!ips.isEmpty()) {
                context.remoteIp = ips.get(0);
                return context.remoteIp;
            }
        }

        return context.remoteIp;
    }

    @Nullable
    public static String getRemoteAddr() {
        val context = getContext();
        if (!StringUtils.hasText(context.remoteAddr)) {
            context.remoteAddr = getRequest().map(HttpServletRequest::getRemoteAddr).orElse(null);
        }

        return context.remoteAddr;
    }

    @Nonnull
    public static String getSchema() {
        val context = getContext();
        if (StringUtils.hasText(context.schema)) {
            return context.schema;
        }
        val request = getRequest();
        var schema = request.map(HttpServletRequest::getScheme).orElse("http");

        var headers = headers();
        val schemas = headers.getFirst(X_FORWARDED_PROTO_HEADER);

        if (StringUtils.hasText(schemas)) {
            val vars = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(schemas);
            if (!vars.isEmpty()) {
                schema = vars.get(0);
            }
        }

        context.schema = schema;
        return schema;
    }

    @Nonnull
    public static String getHostName() {
        val context = getContext();
        if (StringUtils.hasText(context.hostname)) {
            return context.hostname;
        }

        var headers = headers();
        var hostname = headers.getFirst("Host");
        val xForwardHosts = headers.getFirst(X_FORWARDED_HOST_HEADER);

        if (StringUtils.hasText(xForwardHosts)) {
            val vars = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(xForwardHosts);
            if (!vars.isEmpty()) {
                hostname = vars.get(0);
            }
        }

        context.hostname = Optional.ofNullable(hostname).orElse("localhost");
        return hostname;
    }

    @Nonnull
    private static MultiValueMap<String, String> createDefaultHttpHeaders(@Nonnull HttpServletRequest request) {
        final MultiValueMap<String, String> headers = CollectionUtils
            .toMultiValueMap(new LinkedCaseInsensitiveMap<>(8, Locale.ENGLISH));
        for (Enumeration<?> names = request.getHeaderNames(); names.hasMoreElements();) {
            String name = (String) names.nextElement();
            for (Enumeration<?> values = request.getHeaders(name); values.hasMoreElements();) {
                headers.add(name, (String) values.nextElement());
            }
        }
        if (!headers.containsKey("x-request-id")) {
            val rid = request.getAttribute("X-Request-ID");
            if (rid != null) {
                headers.add("X-Request-ID", (String) rid);
            }
            else {
                headers.add("X-Request-ID", UUID.randomUUID().toString());
                request.setAttribute("X-Request-ID", headers.getFirst("X-Request-ID"));
            }
        }
        return headers;
    }

    public final static class Context {

        private String requestId;

        @Getter
        @Setter
        private RequestAttributes attributes;

        private volatile HttpHeaders headers;

        private Map<String, HttpCookie> cookies;

        private String remoteIp;

        private String remoteAddr;

        private String schema;

        private String hostname;

        Context(String requestId, RequestAttributes attributes) {
            this.setRequestId(requestId);
            this.attributes = attributes;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
            if (StringUtils.hasText(requestId)) {
                MDC.put("tid", requestId);
            }
        }

        public void restore() {
            KalamiContextHolder.restore(this);
        }

        public String getRemoteIp() {
            return KalamiContextHolder.getRemoteIp();
        }

        public String getRemoteAddr() {
            return KalamiContextHolder.getRemoteAddr();
        }

        @Nonnull
        public String getSchema() {
            return KalamiContextHolder.getSchema();
        }

        @NonNull
        public static Context current() {
            return KalamiContextHolder.getContext();
        }

    }

}

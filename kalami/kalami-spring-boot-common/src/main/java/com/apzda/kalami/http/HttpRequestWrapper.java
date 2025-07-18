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
package com.apzda.kalami.http;

import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.utils.MultipartBodyUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public class HttpRequestWrapper {

    public static final String BROKER_REQUEST_WRAPPER = "QQ_BROKER_REQ_WRAPPER";

    public static final String CONTENT_CACHING_REQUEST_WRAPPER = "QQ_C_C_REQ_WRAPPER";

    private final ContentCachingRequestWrapper requestWrapper;

    private final ServerRequest delegate;

    private final HttpRequestWrapper parent;

    @Getter
    private final boolean multipart;

    private final Lazy<ObjectMapper> mapperLoader;

    private volatile String requestBody;

    private volatile MultiValueMap<String, HttpEntity<?>> multipartData;

    public HttpRequestWrapper(@Nonnull ServerRequest request) {
        val httpServletRequest = request.servletRequest();
        val attr = request.attribute(BROKER_REQUEST_WRAPPER);
        if (attr.isPresent()) {
            val wrapper = (HttpRequestWrapper) attr.get();
            delegate = wrapper.delegate;
            multipart = wrapper.multipart;
            requestBody = wrapper.requestBody;
            multipartData = wrapper.multipartData;
            requestWrapper = wrapper.requestWrapper;
            parent = wrapper.parent;
        }
        else {
            this.delegate = request;
            this.multipart = StringUtils.startsWithIgnoreCase(httpServletRequest.getContentType(), "multipart/");
            val cachingWrapper = request.attribute(CONTENT_CACHING_REQUEST_WRAPPER);
            if (cachingWrapper.isEmpty()) {
                requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
                request.attributes().put(CONTENT_CACHING_REQUEST_WRAPPER, requestWrapper);
            }
            else {
                requestWrapper = (ContentCachingRequestWrapper) cachingWrapper.get();
            }

            parent = null;
        }
        mapperLoader = Lazy.of(() -> KalamiContextHolder.getBean(ObjectMapper.class));
    }

    @Nonnull
    public static HttpRequestWrapper from(@Nonnull ServerRequest request) {
        val attr = request.attribute(BROKER_REQUEST_WRAPPER);
        if (attr.isPresent()) {
            return (HttpRequestWrapper) attr.get();
        }

        val wrapper = new HttpRequestWrapper(request);
        request.attributes().put(BROKER_REQUEST_WRAPPER, wrapper);

        return wrapper;
    }

    public String getRequestBody() throws IOException {
        if (parent != null) {
            return parent.getRequestBody();
        }
        if (multipart) {
            return null;
        }
        if (requestBody != null) {
            return requestBody;
        }
        if (requestWrapper.getContentLength() == 0) {
            return null;
        }

        if (requestBody == null) {
            synchronized (requestWrapper) {
                if (requestBody == null) {
                    try {
                        if (requestWrapper.getInputStream().isFinished()) {
                            requestBody = StringUtils.defaultIfBlank(requestWrapper.getContentAsString(),
                                    StringUtils.EMPTY);
                        }
                        else {
                            val body = Joiner.on(System.lineSeparator())
                                .join(requestWrapper.getReader().lines().toList());
                            requestBody = StringUtils.defaultIfBlank(body, StringUtils.EMPTY);
                        }
                    }
                    catch (IOException e) {
                        log.warn("Failed to read request body - {} - {}", requestWrapper.getRequestURI(),
                                e.getMessage());
                        throw e;
                    }
                }
            }
        }

        return requestBody;
    }

    public void setRequestBody(@Nonnull String requestBody) {
        if (parent != null) {
            parent.requestBody = requestBody;
        }
        else {
            this.requestBody = requestBody;
        }
    }

    public MultiValueMap<String, HttpEntity<?>> getMultipartData() throws IOException {
        if (parent != null) {
            return parent.getMultipartData();
        }
        if (!multipart) {
            return null;
        }
        if (multipartData == null) {
            synchronized (delegate) {
                if (multipartData == null) {
                    try {
                        multipartData = new LinkedMultiValueMap<>();
                        val data = delegate.multipartData();
                        if (!CollectionUtils.isEmpty(data)) {
                            multipartData = MultipartBodyUtil.fromMap(data).build();
                        }
                    }
                    catch (IOException e) {
                        throw e;
                    }
                    catch (Exception e) {
                        throw new IOException(e.getMessage(), e);
                    }
                }
            }
        }
        return multipartData;
    }

    public void setMultipartData(@Nonnull MultiValueMap<String, HttpEntity<?>> multipartData) {
        if (parent != null) {
            parent.multipartData = multipartData;
        }
        else {
            this.multipartData = multipartData;
        }
    }

    public <T> T getRequestBody(Class<T> clazz) throws IOException {
        return mapperLoader.get().readValue(getRequestBody(), clazz);
    }

}

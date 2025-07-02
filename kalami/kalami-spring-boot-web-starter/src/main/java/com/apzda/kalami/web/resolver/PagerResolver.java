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
package com.apzda.kalami.web.resolver;

import com.apzda.kalami.data.AbstractPageQuery;
import com.apzda.kalami.data.PageRequest;
import com.apzda.kalami.infra.config.InfraConfigProperties;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public class PagerResolver implements HandlerMethodArgumentResolver {

    private final String pageNumber;

    private final String pageSize;

    private final String pageSorts;

    public PagerResolver(@Nonnull InfraConfigProperties config) {
        this.pageNumber = org.apache.commons.lang3.StringUtils.defaultIfBlank(config.getPageParameter(), "pageNumber");
        this.pageSize = org.apache.commons.lang3.StringUtils.defaultIfBlank(config.getSizeParameter(), "pageSize");
        this.pageSorts = org.apache.commons.lang3.StringUtils.defaultIfBlank(config.getSortParameter(), "pageSorts");

    }

    @Override
    public boolean supportsParameter(@Nonnull MethodParameter parameter) {
        return PageRequest.class.equals(parameter.getParameter().getType());
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer,
            @Nonnull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        int pageNumber;
        try {
            pageNumber = Integer.parseInt(StringUtils.defaultIfBlank(webRequest.getParameter(this.pageNumber), "1"));
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid page number: " + this.pageNumber);
        }

        int pageSize;
        try {
            pageSize = Integer.parseInt(StringUtils.defaultIfBlank(webRequest.getParameter(this.pageSize), "20"));
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid page size: " + this.pageSize);
        }

        val ps = webRequest.getParameter(this.pageSorts);

        return new DefaultPageRequest(pageNumber, pageSize, ps);
    }

    static class DefaultPageRequest extends AbstractPageQuery {

        DefaultPageRequest(int pageNumber, int pageSize, String sorts) {
            setPageNumber(pageNumber);
            setPageSize(pageSize);
            setPageSorts(sorts);
        }

    }

}

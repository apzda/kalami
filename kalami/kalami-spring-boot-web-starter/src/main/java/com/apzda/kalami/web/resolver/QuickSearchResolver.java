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

import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.infra.config.InfraConfigProperties;
import com.google.common.base.Splitter;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public class QuickSearchResolver implements HandlerMethodArgumentResolver {

    private final String qsFieldsName;

    private final String qsValueName;

    public QuickSearchResolver(@Nonnull InfraConfigProperties config) {
        this.qsValueName = org.apache.commons.lang3.StringUtils.defaultIfBlank(config.getQvParameter(), "_qv");
        this.qsFieldsName = org.apache.commons.lang3.StringUtils.defaultIfBlank(config.getQfParameter(), "_qf");
    }

    @Override
    public boolean supportsParameter(@Nonnull MethodParameter parameter) {
        return QuickSearch.class.equals(parameter.getParameter().getType());
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer,
            @Nonnull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        val qf = webRequest.getParameter(this.qsFieldsName);
        val qv = webRequest.getParameter(this.qsValueName);
        QuickSearch quickSearch = new QuickSearch();
        quickSearch.setKeyword(qv);
        if (StringUtils.isNotBlank(qf)) {
            quickSearch.setFields(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(qf));
        }
        else {
            quickSearch.setFields(Collections.emptyList());
        }

        return quickSearch;
    }

}

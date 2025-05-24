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
package com.apzda.kalami.web.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class KalamiErrorViewResolver extends DefaultErrorViewResolver {

    public KalamiErrorViewResolver(ApplicationContext applicationContext, WebProperties.Resources resources) {
        super(applicationContext, resources);
    }

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        val mv = super.resolveErrorView(request, status, model);
        if (mv != null) {
            return mv;
        }

        val modelAndView = new ModelAndView(new BlankErrorView());
        modelAndView.addAllObjects(model);

        return modelAndView;
    }

}

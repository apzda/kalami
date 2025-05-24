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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class KalamiErrorController extends BasicErrorController {

    private final ErrorAttributes errorAttributes;

    @Value("${kalami.security.login-url:}")
    private String loginUrl;

    public KalamiErrorController(ErrorAttributes errorAttributes, ErrorProperties errorProperties,
            List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, errorProperties, errorViewResolvers);
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        val webRequest = new ServletWebRequest(request);

        Throwable error = errorAttributes.getError(webRequest);

        if (error != null) {
            while (error instanceof ServletException && error.getCause() != null) {
                error = error.getCause();
            }

            if (StringUtils.isNotBlank(loginUrl)) {
                val redirectView = new ModelAndView(new RedirectView(loginUrl));
                if (error instanceof HttpStatusCodeException codeException
                        && codeException.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    return redirectView;
                }
            }
        }

        Map<String, Object> model = Collections
            .unmodifiableMap(getErrorAttributes(webRequest, getErrorAttributeOptions(request, MediaType.TEXT_HTML)));
        response.setStatus(status.value());

        ModelAndView modelAndView = resolveErrorView(request, response, status, model);

        return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
    }

    @Override
    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }
        val webRequest = new ServletWebRequest(request);
        Map<String, Object> body = getErrorAttributes(webRequest, getErrorAttributeOptions(request, MediaType.ALL));
        return ResponseEntity.status(status).body(body);
    }

    protected Map<String, Object> getErrorAttributes(ServletWebRequest request, ErrorAttributeOptions options) {
        return this.errorAttributes.getErrorAttributes(request, options);
    }

}

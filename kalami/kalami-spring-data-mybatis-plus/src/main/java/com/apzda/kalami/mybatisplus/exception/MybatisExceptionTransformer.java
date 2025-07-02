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
package com.apzda.kalami.mybatisplus.exception;

import com.apzda.kalami.http.ExceptionTransformer;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.regex.Pattern;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public class MybatisExceptionTransformer implements ExceptionTransformer {

    private final static Pattern pattern = Pattern.compile("Duplicate entry '(.*?)' for key",
            Pattern.MULTILINE | Pattern.DOTALL);

    private final static Pattern truncatePattern = Pattern.compile("Data truncation: Data too long for column '(.*?)' ",
            Pattern.MULTILINE | Pattern.DOTALL);

    @Override
    public Exception transform(@Nonnull Throwable exception) {
        val message = exception.getMessage();
        val matcher = pattern.matcher(StringUtils.trim(message));
        ProblemDetail problemDetail;
        if (matcher.find()) {
            problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("【'%s'】已存在", matcher.group(1)));
        }
        else {
            val m2 = truncatePattern.matcher(StringUtils.trim(message));
            if (m2.find()) {
                problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                        String.format("字段【%s】的值超过允许长度", matcher.group(1)));
            }
            else {
                problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "系统暂不可用[DB]");
            }
        }

        log.error("数据错误，无法落库: {}", exception.getClass(), exception);
        return new ErrorResponseException(HttpStatus.INTERNAL_SERVER_ERROR, problemDetail, exception);
    }

    @Override
    public boolean supports(Class<? extends Throwable> eClass) {
        return DataIntegrityViolationException.class.isAssignableFrom(eClass);
    }

}

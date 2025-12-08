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
package com.apzda.kalami.user;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.lang.annotation.*;
import java.util.Collections;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Data
@Builder
public class CurrentUser implements Serializable {

    @Serial
    private static final long serialVersionUID = -8353034394933412588L;

    private String uid;

    private String runAs;

    private String app;

    private String device;

    private String userAgent;

    private String remoteAddress;

    private String spm;

    private Map<String, String> meta;

    private boolean authenticated;

    public Map<String, String> getMeta() {
        if (CollectionUtils.isEmpty(meta)) {
            return Collections.emptyMap();
        }
        return meta;
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Required {

        boolean value() default true;

    }

}

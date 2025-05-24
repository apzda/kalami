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
package com.apzda.kalami.cloud.openfeign.i18n;

import com.apzda.kalami.i18n.BaseNameResolver;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
public class FeignMessageSourceNameResolver implements BaseNameResolver {

    @Override
    public List<String> getBaseNames() {
        return List.of("kalami.feign.messages");
    }

}

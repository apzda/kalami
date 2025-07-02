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
package com.apzda.kalami.error;

import com.apzda.kalami.data.error.AbstractBaseError;
import com.apzda.kalami.i18n.I18n;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class ResourceCreateError extends AbstractBaseError {

    public ResourceCreateError(String resource) {
        if (resource != null && resource.startsWith("{") && resource.endsWith("}")) {
            resource = resource.substring(1, resource.length() - 1);
            resource = I18n.t(resource);
        }

        this.args = new Object[] { resource };
    }

    @Override
    public int code() {
        return 601;
    }

}

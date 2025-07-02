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
package com.apzda.kalami.tenant;

import com.apzda.kalami.dictionary.Transformer;
import lombok.val;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class SubscriptionTransformer implements Transformer<String> {

    @Override
    public Object transform(String value, boolean all) {
        val subs = TenantManager.availableSubscriptions();

        val obj = subs.get(value);
        if (obj == null) {
            return null;
        }
        if (all) {
            return obj;
        }

        return obj.getName();
    }

}

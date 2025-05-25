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
package com.apzda.kalami.data;

import com.google.common.base.Splitter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */

public interface PageRequest {

    int pageNumber();

    int pageSize();

    String pageSorts();

    default Pageable toPageable() {
        Sort sort;
        val sortFields = pageSorts();
        if (StringUtils.isNotBlank(sortFields)) {
            val orders = new ArrayList<Sort.Order>();
            for (String sortField : Splitter.on(",")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(UriUtils.decode(sortFields, StandardCharsets.UTF_8))) {
                val sorts = sortField.split("\\|");
                if (sorts.length > 1) {
                    if ("asc".equalsIgnoreCase(sorts[1])) {
                        orders.add(Sort.Order.asc(sorts[0]));
                    }
                    else {
                        orders.add(Sort.Order.desc(sorts[0]));
                    }
                }
                else {
                    orders.add(Sort.Order.asc(sorts[0]));
                }
            }
            if (orders.isEmpty()) {
                throw new IllegalArgumentException("Invalid sort order: " + sortFields);
            }
            else {
                sort = Sort.by(orders);
            }
        }
        else {
            sort = Sort.unsorted();
        }

        Pageable pageable;
        if (pageNumber() < 0) {
            pageable = Pageable.unpaged(sort);
        }
        else {
            pageable = org.springframework.data.domain.PageRequest.of(pageNumber(), pageSize(), sort);
        }

        return pageable;
    }

}

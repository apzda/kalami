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
package com.apzda.kalami.demo.store.controller.feign;

import com.apzda.kalami.demo.store.feign.StoreDetailService;
import com.apzda.kalami.demo.store.feign.StoreService;
import com.apzda.kalami.security.annotation.Authenticated;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@RestController
@Primary
public class StoreServiceImpl implements StoreService, StoreDetailService {

    @Authenticated
    @Override
    public Integer count(@Validated @NotBlank String sku) {
        return 110;
    }

    @PreAuthorize("hasRole('USER')")
    @Override
    public Integer countDetail(String sku) {
        return 120;
    }

}

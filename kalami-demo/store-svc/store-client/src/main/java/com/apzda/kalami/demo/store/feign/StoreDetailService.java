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
package com.apzda.kalami.demo.store.feign;

import jakarta.validation.constraints.NotBlank;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@FeignClient(name = "${kalami.cloud.feign.service.store.name:storeSvc}",
        url = "${kalami.cloud.feign.service.store.url:}",
        contextId = "com.apzda.kalami.demo.store.feign.StoreDetailService", primary = false)
public interface StoreDetailService {

    @GetMapping("/_/storeSvc/store/countDetail/{sku}")
    Integer countDetail(@Validated @NotBlank @PathVariable String sku);

}

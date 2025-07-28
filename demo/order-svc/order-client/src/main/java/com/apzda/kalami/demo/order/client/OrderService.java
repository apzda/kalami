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
package com.apzda.kalami.demo.order.client;

import com.apzda.kalami.demo.order.client.dto.OrderDto;
import com.apzda.kalami.demo.order.client.vo.OrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
//@formatter:off
@FeignClient(
    name = "${kalami.cloud.feign.service.order.name:orderSvc}",
    url = "${kalami.cloud.feign.service.order.url:}",
    contextId = "kalami.cloud.feign.service.OrderService",
    primary = false
)
//@formatter:on
public interface OrderService {

    /**
     * 创建订单
     */
    @PostMapping("/_/orderSvc/createOrder")
    OrderVo createOrder(@RequestBody OrderDto dto);

}

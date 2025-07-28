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
package com.apzda.kalami.demo.order.controller.internal;

import com.apzda.kalami.demo.order.client.OrderService;
import com.apzda.kalami.demo.order.client.dto.OrderDto;
import com.apzda.kalami.demo.order.client.vo.OrderVo;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RestController
public class OrderServiceController implements OrderService {

    @Override
    public OrderVo createOrder(@Validated OrderDto dto) {
        val vo = new OrderVo();
        vo.setAmount(dto.getAmount());
        return vo;
    }

}

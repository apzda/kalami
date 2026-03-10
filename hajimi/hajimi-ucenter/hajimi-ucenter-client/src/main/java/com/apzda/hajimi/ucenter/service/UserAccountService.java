/*
 * Copyright 2026 the original author or authors.
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
package com.apzda.hajimi.ucenter.service;

import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.hajimi.ucenter.service.dto.RefundDto;
import com.apzda.hajimi.ucenter.service.dto.TradeDto;
import com.apzda.hajimi.ucenter.service.dto.TransitionDto;
import com.apzda.hajimi.ucenter.service.vo.RefundVo;
import com.apzda.hajimi.ucenter.service.vo.TransactionVo;
import com.apzda.hajimi.ucenter.service.vo.TransitionVo;
import com.apzda.hajimi.ucenter.service.vo.UserAccountVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
//@formatter:off
@FeignClient(
    name = "${kalami.cloud.feign.service.ucenter.name:ucenterSvc}",
    url = "${kalami.cloud.feign.service.ucenter.url:}",
    contextId = "kalami.cloud.feign.service.UCenterUserAccountService",
    primary = false
)
//@formatter:on
public interface UserAccountService {

    @GetMapping("/_/ucenterSvc/account/{asset}/{uid}")
    Optional<UserAccountVo> getUserAccount(@PathVariable Asset asset, @PathVariable String uid,
            @RequestParam(value = "tid", required = false) String tenantId);

    /**
     * 获取租户超级管理员的账户
     */
    @PostMapping("/_/ucenterSvc/listUserAccount/{asset}")
    List<UserAccountVo> listUserAccount(@PathVariable Asset asset, @RequestBody List<String> tenantIds);

    /**
     * 交易
     */
    @PostMapping("/_/ucenterSvc/account/trade")
    Optional<TransactionVo> trade(@RequestBody TradeDto trade);

    /**
     * 退款
     */
    @PostMapping("/_/ucenterSvc/account/refund")
    Optional<RefundVo> refund(@RequestBody RefundDto dto);

    /**
     * 取消预交易
     */
    @PostMapping("/_/ucenterSvc/account/cancel")
    Optional<TransitionVo> cancel(@RequestBody TransitionDto dto);

    /**
     * 确认预交易
     */
    @PostMapping("/_/ucenterSvc/account/confirm")
    Optional<TransitionVo> confirm(@RequestBody TransitionDto dto);

}

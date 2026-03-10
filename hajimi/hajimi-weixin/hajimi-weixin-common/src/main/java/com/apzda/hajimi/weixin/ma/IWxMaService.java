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
package com.apzda.hajimi.weixin.ma;

import cn.binarywang.wx.miniapp.bean.express.WxMaExpressDelivery;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.WxMaOrderCombinedShippingInfoUploadRequest;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.WxMaOrderShippingInfoUploadRequest;
import cn.binarywang.wx.miniapp.bean.shop.response.WxMaOrderShippingInfoBaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
//@formatter:off
@FeignClient(
    name = "${kalami.cloud.feign.service.ucenter.name:ucenterSvc}",
    url = "${kalami.cloud.feign.service.ucenter.url:}",
    contextId = "kalami.cloud.feign.service.IWxMaService",
    primary = false
)
//@formatter:on
public interface IWxMaService {

    /**
     * 发货信息录入接口
     */
    @PostMapping(value = "/_/ucenterSvc/wxMap/shipping/{appId}")
    WxMaOrderShippingInfoBaseResponse uploadShippingInfo(@PathVariable String appId,
            @RequestBody WxMaOrderShippingInfoUploadRequest shippingInfo);

    /**
     * 发货信息合单录入接口
     */
    @PostMapping(value = "/_/ucenterSvc/wxMap/combinedShipping/{appId}")
    WxMaOrderShippingInfoBaseResponse uploadCombinedShippingInfo(@PathVariable String appId,
            @RequestBody WxMaOrderCombinedShippingInfoUploadRequest shippingInfo);

    /**
     * 获取支持的快递公司列表
     */
    @GetMapping("/_/ucenterSvc/wxMap/listDeliveries")
    List<WxMaExpressDelivery> listDeliveries();

}

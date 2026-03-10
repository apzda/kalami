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
package com.apzda.hajimi.ucenter.wx.mini.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.express.WxMaExpressDelivery;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.WxMaOrderCombinedShippingInfoUploadRequest;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.WxMaOrderShippingInfoUploadRequest;
import cn.binarywang.wx.miniapp.bean.shop.response.WxMaOrderShippingInfoBaseResponse;
import com.apzda.hajimi.weixin.ma.IWxMaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Primary
public class WxMiniInternalController implements IWxMaService {

    private final WxMaService wxMaService;

    @Override
    public WxMaOrderShippingInfoBaseResponse uploadShippingInfo(String appId,
            @RequestBody WxMaOrderShippingInfoUploadRequest shippingInfo) {
        val service = wxMaService.switchoverTo(appId);
        try {
            return service.getWxMaOrderShippingService().upload(shippingInfo);
        }
        catch (Exception e) {
            throw new IllegalStateException("发货信息录入出错: " + e.getMessage(), e);
        }
    }

    @Override
    public WxMaOrderShippingInfoBaseResponse uploadCombinedShippingInfo(String appId,
            @RequestBody WxMaOrderCombinedShippingInfoUploadRequest shippingInfo) {
        val service = wxMaService.switchoverTo(appId);
        try {
            return service.getWxMaOrderShippingService().upload(shippingInfo);
        }
        catch (Exception e) {
            throw new IllegalStateException("发货信息合单录入: " + e.getMessage(), e);
        }
    }

    @Override
    public List<WxMaExpressDelivery> listDeliveries() {
        val service = wxMaService.switchoverTo("");
        try {
            return service.getExpressService().getAllDelivery();
        }
        catch (Exception e) {
            log.error("获取支持的快递公司列表失败: {}", e.getMessage());
            return List.of();
        }
    }

}

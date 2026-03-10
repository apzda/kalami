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
package com.apzda.hajimi.dy.vo;

import com.apzda.kalami.user.CurrentUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShareVo {

    /**
     * 视频归属ID
     */
    private String id;

    /**
     * 视频ID
     */
    private String vId;

    /**
     * 分享类型
     */
    private String type;

    /**
     * Client Key
     */
    private String clientKey;

    /**
     * 随机字符串
     */
    private String nonceStr;

    /**
     * 秒级时间戳
     */
    private String timestamp;

    /**
     * 签名
     */
    private String signature;

    /**
     * 分享ID
     */
    private String shareId;

    /**
     * 小程序
     */
    private String appId;

    /**
     * 小程序地址
     */
    private String appUrl;

    /**
     * 小程序名称
     */
    private String appTitle;

    /**
     * 小程序描述
     */
    private String appDesc;

    /**
     * 分享用户
     */
    @JsonIgnore
    private CurrentUser user;

}

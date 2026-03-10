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
package com.apzda.hajimi.weixin.mp;

import com.apzda.hajimi.weixin.WxConfiguration;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class WxMpConfiguration implements WxConfiguration {

    /**
     * 设置微信公众号的appid
     */
    @NotBlank
    private String appid;

    /**
     * 设置微信公众号的Secret
     */
    @NotBlank
    private String secret;

    /**
     * 设置微信公众号消息服务器配置的token
     */
    private String token;

    /**
     * 设置微信公众号消息服务器配置的EncodingAESKey
     */
    private String aesKey;

    /**
     * 消息模板
     */
    private Map<String, String> templates;

}

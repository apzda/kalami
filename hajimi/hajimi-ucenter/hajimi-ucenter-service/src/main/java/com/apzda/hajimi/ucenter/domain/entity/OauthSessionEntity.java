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
package com.apzda.hajimi.ucenter.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 登录会话
 */
@TableName(value = "ucenter_oauth_session", autoResultMap = true)
@Data
public class OauthSessionEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableLogic
    private Boolean deleted;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 认证ID
     */
    private Long oauthId;

    /**
     * 认证提供者
     */
    private String provider;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 授权码
     */
    private String grantCode;

    /**
     * access token
     */
    private String accessToken;

    /**
     * refresh token
     */
    private String refreshToken;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expiredAt;

    /**
     * OpenID
     */
    private String openId;

    /**
     * UnionID
     */
    private String unionId;

    /**
     * 是否是模拟器
     */
    private Boolean simulator;

    /**
     * 设备
     */
    private String device;

    /**
     * 登录IP
     */
    private String ip;

    /**
     * 代理
     */
    private String userAgent;

    /**
     * spm
     */
    private String spm;

    /**
     * 额外数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> extra;

}

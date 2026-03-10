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
import lombok.Data;

import java.io.Serializable;

/**
 * 用户认证列表
 */
@TableName(value = "ucenter_oauth")
@Data
@KeySequence
public class OauthEntity implements Serializable {

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
     * User ID
     */
    private Long uid;

    /**
     * 认证提供者
     */
    private String provider;

    /**
     * appId
     */
    private String appId;

    /**
     * OpenID
     */
    private String openId;

    /**
     * UnionID
     */
    private String unionId;

    /**
     * 第一次登录时间
     */
    private Long loginTime;

    /**
     * 推荐码
     */
    private String recCode;

    /**
     * 第一次登录设备
     */
    private String device;

    /**
     * the ip address(v4 or v6) from which the user login
     */
    private String ip;

    /**
     * user agent
     */
    private String userAgent;

    /**
     * spm
     */
    private String spm;

    /**
     * remark
     */
    private String remark;

}

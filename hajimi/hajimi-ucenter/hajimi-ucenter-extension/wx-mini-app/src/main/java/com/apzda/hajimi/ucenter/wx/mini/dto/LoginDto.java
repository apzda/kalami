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
package com.apzda.hajimi.ucenter.wx.mini.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class LoginDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1809233488815323983L;

    /**
     * 授权代码
     */
    @NotBlank(message = "授权码不能为空")
    private String code;

    /**
     * 获取手机号授权号
     */
    private String phoneCode;

    /**
     * 门店ID
     */
    private String shopId;

    /**
     * 推荐码
     */
    private String recCode;

    /**
     * 来源追踪参数
     */
    private String spm;

}

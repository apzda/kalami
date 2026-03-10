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
package com.apzda.hajimi.ucenter.sms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.val;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class LoginDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -4384735810909888591L;

    private static final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^\\+86\\s*");

    /**
     * 手机号
     */
    @Pattern(regexp = "^(\\+\\d{2,5}\\s+)?1\\d{10}$", message = "手机号不正确")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

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

    public String getPhone() {
        if (this.phone == null) {
            return null;
        }

        val matcher = pattern.matcher(this.phone);
        if (matcher.find()) {
            return this.phone.replaceAll("^\\+86\\s*", "");
        }

        return this.phone;
    }

}

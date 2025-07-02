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

package com.apzda.kalami.security.token;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultToken implements JwtToken, Serializable {

    @Serial
    private static final long serialVersionUID = -2763131228048354173L;

    /**
     * 用户ID（用户实体ID）
     */
    private String uid;

    /**
     * 以用户身份运行时，主用户ID
     */
    private String runas;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * MFA状态(2位，4种状态)
     */
    private Integer mfa;

    /**
     * 用户状态(4位,16种状态)
     */
    private String status;

    /**
     * 是否锁定
     */
    private Boolean locked;

    /**
     * 账户是否激活
     */
    private Boolean deactivated;

    /**
     * 账户是否绑定到主账户
     */
    private Boolean unbound;

    /**
     * 凭证(密码、证书等)是否过期
     */
    private Boolean credentialsExpired;

    /**
     * 账户是否过期
     */
    private Boolean expired;

    /**
     * 账户是否被禁用
     */
    private Boolean disabled;

}

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
package com.apzda.hajimi.ucenter.token;

import com.apzda.kalami.security.token.JwtToken;
import com.apzda.kalami.tenant.Organization;
import com.apzda.kalami.tenant.Tenant;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserToken implements JwtToken, Serializable {

    public static final String DEFAULT_PROVIDER = "sys";

    public static final String DEFAULT_APPID = "";

    @Serial
    private static final long serialVersionUID = -2763131228048354173L;

    /**
     * 用户ID（用户实体ID）
     */
    private String uid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String displayName;

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

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 最后登录时间
     */
    private Long lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 落地页
     */
    private String landingUrl;

    /**
     * 角色列表
     */
    private List<Role> roles;

    /**
     * 权限列表
     */
    private List<String> authorities;

    /**
     * 当前租户
     */
    private Tenant tenant;

    /**
     * 所属租户列表
     */
    private List<Tenant> tenants;

    /**
     * 当前组织
     */
    private Organization org;

    /**
     * 所属组织列表
     */
    private List<Organization> organizations;

    /**
     * 附加
     */
    private Map<String, Object> extra;

    public List<String> getAuthorities() {
        if (CollectionUtils.isEmpty(authorities)) {
            this.authorities = new ArrayList<>();
        }

        return authorities;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Role implements Serializable {

        private Serializable id;

        private String name;

        private String role;

    }

}

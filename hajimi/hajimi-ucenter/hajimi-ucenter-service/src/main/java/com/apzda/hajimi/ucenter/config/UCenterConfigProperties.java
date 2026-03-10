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
package com.apzda.hajimi.ucenter.config;

import com.apzda.hajimi.ucenter.mfa.Authenticator;
import com.apzda.hajimi.ucenter.security.mfa.GoogleTotpAuthenticator;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "hajimi.ucenter")
public class UCenterConfigProperties {

    /**
     * 登录时禁用验证码
     */
    private boolean captchaDisabled;

    /**
     * 第三方登录时是否自动创建账户，如果不创建则需要绑定到已有账户
     */
    private boolean autoCreateAccount = true;

    /**
     * 新用户默认昵称前缀
     */
    private String nickNamePrefix = "用户";

    /**
     * 新用户账号前缀
     */
    private String userNamePrefix = "u-";

    /**
     * 多因素认证器
     */
    private Class<? extends Authenticator> authenticator = GoogleTotpAuthenticator.class;

    /**
     * 多因素认证器配置属性
     */
    private final Map<String, Object> props = new LinkedHashMap<>();

}

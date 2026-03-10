/*
 * Copyright 2025-2026 the original author or authors.
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

import com.apzda.hajimi.ucenter.domain.repository.OauthRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.mfa.Authenticator;
import com.apzda.hajimi.ucenter.mfa.NoneAuthenticator;
import com.apzda.hajimi.ucenter.security.authentication.UsernameAndPasswordProvider;
import com.apzda.hajimi.ucenter.security.mfa.GoogleTotpAuthenticator;
import com.apzda.kalami.io.YamlPropertySourceFactory;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.util.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@MapperScan("com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper")
@EnableConfigurationProperties({ UCenterConfigProperties.class })
@RequiredArgsConstructor
@PropertySources({ @PropertySource(name = "ucenter-security-config", value = "classpath:/ucenter-security-config.yml",
        factory = YamlPropertySourceFactory.class) })
public class UCenterServiceConfiguration {

    private final ApplicationContext applicationContext;

    @Getter
    private final UCenterConfigProperties properties;

    @Bean
    UsernameAndPasswordProvider usernameAndPasswordAuthenticationProvider(UserRepository userRepository,
            OauthRepository oauthRepository, PasswordEncoder passwordEncoder) {
        return new UsernameAndPasswordProvider(userRepository, oauthRepository, passwordEncoder);
    }

    @Bean
    Authenticator googleTotpAuthenticator() {
        return new GoogleTotpAuthenticator();
    }

    /**
     * 多因素认证器.
     * @return 多因素认证器实现.
     */
    @Nonnull
    public Lazy<Authenticator> getAuthenticator() {
        val authenticatorClz = properties.getAuthenticator();
        if (authenticatorClz == null) {
            return Lazy.of(new NoneAuthenticator());
        }
        try {
            return Lazy.of(() -> applicationContext.getBean(authenticatorClz));
        }
        catch (Exception e) {
            return Lazy.of(new NoneAuthenticator());
        }

    }

}

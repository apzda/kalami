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

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.exception.TokenException;
import com.apzda.kalami.security.user.MetaUserDetailsService;
import com.apzda.kalami.security.utils.SecurityUtils;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/18
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenManager implements TokenManager {

    private final static String PAYLOAD_UID = "i";

    private final static String PAYLOAD_FLAG = "f";

    private final static String PAYLOAD_RUNAS = "s";

    private final MetaUserDetailsService metaUserDetailsService;

    private final ObjectProvider<JwtTokenCustomizer> customizers;

    private final SecurityConfigProperties properties;

    private final JWTSigner jwtSigner;

    /**
     * 从accessToken(JWT)中恢复认证
     * @param accessToken JWT
     */
    @Override
    public Authentication restore(String accessToken) {
        boolean verified;
        try {
            verified = JWTUtil.verify(accessToken, jwtSigner);
        }
        catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.trace("accessToken is invalid: {}", accessToken, e);
            }
            throw TokenException.INVALID_TOKEN;
        }

        if (verified) {
            val jwt = JWTUtil.parseToken(accessToken);
            jwt.setSigner(jwtSigner);
            val jwtLeeway = properties.getJwtLeeway();

            if (!jwt.validate(jwtLeeway.toSeconds())) {
                log.trace("accessToken is expired: {}", accessToken);
                throw TokenException.EXPIRED;
            }

            val token = DefaultToken.builder()
                .accessToken(accessToken)
                .uid((String) jwt.getPayload(JWT.SUBJECT))
                .build();

            if (jwt.getPayload(PAYLOAD_RUNAS) != null) {
                token.setRunAs((String) jwt.getPayload(PAYLOAD_RUNAS));
            }

            if (jwt.getPayload(PAYLOAD_FLAG) != null) {
                JwtToken.parseFlag(Integer.parseInt((String) jwt.getPayload(PAYLOAD_FLAG)), token);
            }

            val uid = token.getUid();
            val userBuilder = User.withUsername(uid)
                .password("")
                .accountLocked(token.getLocked())
                .accountExpired(token.getExpired())
                .credentialsExpired(token.getCredentialsExpired())
                .disabled(token.getDisabled());

            val userDetails = metaUserDetailsService.create(userBuilder.build());
            val authentication = JwtTokenAuthentication.authenticated(userDetails, "");

            // 此处不验证该accessToken是否被收回。请在网关层判断!
            token.setAccessToken(accessToken);
            authentication.setJwtToken(token);
            verify(authentication);

            log.trace("Authentication is restored from accessToken: {}", accessToken);
            return authentication;
        }
        else {
            log.trace("accessToken is invalid: {}", accessToken);
            throw TokenException.INVALID_TOKEN;
        }
    }

    /**
     * 根据认证创建一个{@link JwtToken}实例.
     * <p/>
     * 通过{@link JwtTokenCustomizer}对新建的实例进行定制.
     * @param authentication 认证
     */
    @Override
    public JwtToken create(JwtToken oldToken, @Nonnull Authentication authentication) {
        val name = authentication.getName();
        JwtToken jwtToken = DefaultToken.builder().uid(name).build();

        if (oldToken != null) {
            jwtToken = oldToken;
        }
        else {
            val cs = customizers.orderedStream().toList();
            for (val c : cs) {
                jwtToken = c.customize(authentication, jwtToken);
            }
        }

        val principal = authentication.getPrincipal();
        if (principal instanceof UserDetails ud) {
            jwtToken.setLocked(!ud.isAccountNonLocked());
            jwtToken.setCredentialsExpired(!ud.isCredentialsNonExpired());
            jwtToken.setDisabled(!ud.isEnabled());
            jwtToken.setExpired(!ud.isAccountNonExpired());
        }

        jwtToken.setAccessToken(createAccessToken(jwtToken));
        jwtToken.setRefreshToken(createRefreshToken(jwtToken, authentication));

        return jwtToken;
    }

    /**
     * 刷新
     * @param jwtToken 要刷新的Token实例
     * @return 新的Token实例
     */
    @Override
    public JwtToken refresh(@Nonnull JwtToken jwtToken, UserDetails userDetails) {
        return refresh(userDetails, jwtToken).getJwtToken();
    }

    /**
     * 验证当前认证会话是否有效
     */
    @Override
    public void verify(@Nonnull Authentication authentication) {
        log.trace("verified: {}", authentication);
    }

    /**
     * 刷新认证
     * @param jwtToken 要刷新的Token实例
     * @return 新的认证
     */
    @Nonnull
    private JwtTokenAuthentication refresh(UserDetails userDetails, @Nonnull JwtToken jwtToken) {
        val uid = jwtToken.getUid();
        if (StringUtils.isBlank(uid)) {
            log.trace("refreshToken is invalid: {} - uid is blank", jwtToken.getRefreshToken());
            throw TokenException.INVALID_TOKEN;
        }

        val refreshToken = jwtToken.getRefreshToken();
        if (StringUtils.isBlank(refreshToken)) {
            log.trace("refreshToken is empty!");
            throw TokenException.INVALID_TOKEN;
        }

        try {
            JWTUtil.verify(refreshToken, jwtSigner);
        }
        catch (Exception e) {
            log.trace("refreshToken is invalid: {} - {}", refreshToken, e.getMessage());
            throw TokenException.INVALID_TOKEN;
        }

        val jwt = JWTUtil.parseToken(refreshToken);
        jwt.setSigner(jwtSigner);

        val jwtLeeway = properties.getJwtLeeway();
        if (!jwt.validate(jwtLeeway.toSeconds())) {
            log.trace("refreshToken is expired: {}", refreshToken);
            throw TokenException.EXPIRED;
        }

        // 验证合法性
        SecurityUtils.checkUserDetails(userDetails);

        val accessToken = StringUtils.defaultString(jwtToken.getAccessToken());
        // 当前用户的凭证
        val password = userDetails.getPassword();
        val oldSign = (String) jwt.getPayload(JWT.SUBJECT);
        val sign = MD5.create().digestHex(accessToken + password);

        if (Objects.equals(oldSign, sign)) {
            val authentication = JwtTokenAuthentication.unauthenticated(userDetails, userDetails.getPassword());
            val newToken = create(jwtToken, authentication);
            authentication.setJwtToken(newToken);
            return authentication;
        }

        log.trace("refreshToken is invalid: {} - accessToken or password does not match", refreshToken);
        throw TokenException.INVALID_TOKEN;
    }

    private String createAccessToken(@Nonnull JwtToken jwtToken) {
        val token = JWT.create();

        if (StringUtils.isNotBlank(jwtToken.getRunAs())) {
            token.setPayload(PAYLOAD_RUNAS, jwtToken.getRunAs());
        }

        val flag = jwtToken.getFlag();
        if (!"0".equals(flag)) {
            token.setPayload(PAYLOAD_FLAG, flag);
        }
        // 是用户ID
        token.setSubject(jwtToken.getUid());
        token.setSigner(jwtSigner);
        val accessExpireAt = DateUtil.date()
            .offset(DateField.MINUTE, (int) properties.getAccessTokenTimeout().toMinutes());
        token.setExpiresAt(accessExpireAt);

        return token.sign();
    }

    /**
     * 基于JwtToken和认证创建刷新Token
     * @param jwtToken Token
     * @param authentication 认证
     * @return accessToken
     */
    @Nonnull
    private String createRefreshToken(@Nonnull JwtToken jwtToken, @Nonnull Authentication authentication) {
        val accessToken = jwtToken.getAccessToken();
        val principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            val password = userDetails.getPassword();
            val expire = properties.getRefreshTokenTimeout();
            val accessExpireAt = DateUtil.date().offset(DateField.MINUTE, (int) expire.toMinutes());
            val token = JWT.create();
            val refreshToken = MD5.create().digestHex(accessToken + password);
            token.setSubject(refreshToken);
            if (StringUtils.isNotBlank(jwtToken.getUid())) {
                token.setPayload(PAYLOAD_UID, jwtToken.getUid());
            }

            if (StringUtils.isNotBlank(jwtToken.getRunAs())) {
                token.setPayload(PAYLOAD_RUNAS, jwtToken.getRunAs());
            }

            val flag = jwtToken.getFlag();
            if ("0".equals(flag)) {
                token.setPayload(PAYLOAD_FLAG, flag);
            }

            token.setExpiresAt(accessExpireAt);
            token.setSigner(jwtSigner);
            return token.sign();
        }

        return "";
    }

}

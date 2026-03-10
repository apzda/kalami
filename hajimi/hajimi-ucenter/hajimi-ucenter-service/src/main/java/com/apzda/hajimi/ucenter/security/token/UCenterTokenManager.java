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
package com.apzda.hajimi.ucenter.security.token;

import cn.hutool.crypto.digest.MD5;
import cn.hutool.jwt.signers.JWTSigner;
import com.apzda.hajimi.ucenter.security.data.AuthData;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.apzda.kalami.security.authentication.AuthenticationDetails;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.exception.InvalidSessionException;
import com.apzda.kalami.security.exception.TokenException;
import com.apzda.kalami.security.token.JwtToken;
import com.apzda.kalami.security.token.JwtTokenCustomizer;
import com.apzda.kalami.security.token.JwtTokenManager;
import com.apzda.kalami.security.user.MetaUserDetailsService;
import com.apzda.kalami.service.TempStorageService;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Component
public class UCenterTokenManager extends JwtTokenManager {

    private final SecurityConfigProperties securityConfigProperties;

    private final TempStorageService tempStorageService;

    public UCenterTokenManager(MetaUserDetailsService metaUserDetailsService,
            ObjectProvider<JwtTokenCustomizer> customizers, SecurityConfigProperties properties, JWTSigner jwtSigner,
            TempStorageService tempStorageService, SecurityConfigProperties securityConfigProperties) {
        super(metaUserDetailsService, customizers, properties, jwtSigner);
        this.tempStorageService = tempStorageService;
        this.securityConfigProperties = securityConfigProperties;
    }

    @Override
    public void verify(@Nonnull Authentication authentication) {
        val authId = getAuthId(authentication);
        val authData = tempStorageService.load(authId, AuthData.class);
        val message = authData.map(AuthData::getMessage).orElse(null);

        if (StringUtils.isNotBlank(message)) {
            throw new InvalidSessionException(message);
        }

        if (authentication instanceof JwtTokenAuthentication jwtAuth) {
            // Token无效检测
            val token = jwtAuth.getJwtToken();
            val accessToken = token.getAccessToken();
            val acId = MD5.create().digestHex(accessToken);

            try {
                if (tempStorageService.exist(acId)) {
                    throw new InvalidSessionException();
                }
            }
            catch (Exception e) {
                log.warn("检测AccessToken合法性失败，做失效处理: {}", accessToken);
                throw TokenException.EXPIRED;
            }

            val details = (AuthenticationDetails) jwtAuth.getDetails();
            val app = details.getApp();

            if (StringUtils.isNotBlank(app) && securityConfigProperties.getApp().containsKey(app)) {
                val config = securityConfigProperties.getApp().get(app);
                if (!config.isMultiPointEnabled()) {
                    val id = "login." + getAuthId(authentication);
                    val ad = tempStorageService.load(id, AuthData.class);
                    val ac = MD5.create().digestHex(accessToken);
                    if (ad.isPresent() && !ac.equals(ad.get().getToken())) {
                        throw new InvalidSessionException("您的账号已在它处登录");
                    }
                }
            }
        }
    }

    @Override
    public void remove(@Nonnull Authentication authentication) {
        remove(getAuthId(authentication));

        if (authentication instanceof JwtTokenAuthentication jwtAuth) {
            val details = (AuthenticationDetails) jwtAuth.getDetails();
            val token = jwtAuth.getJwtToken();
            invalidAccessToken(token, details);
        }
    }

    public void remove(String uid, String provider, String appId) {
        remove(getAuthId(uid, provider, appId));
    }

    /**
     * 标记当前AccessToken不可用
     */
    public void invalidAccessToken(@Nonnull JwtToken token, @Nonnull AuthenticationDetails details) {
        val accessToken = token.getAccessToken();
        val acId = MD5.create().digestHex(accessToken);
        val duration = securityConfigProperties.accessTokenTimeout(details.getApp()).plusMinutes(10);
        val data = new AuthData().setExpireTime(duration);

        try {
            tempStorageService.save(acId, data);
        }
        catch (Exception e) {
            log.error("Cannot mark the accessToken of '{}' invalid - {}", token.getUid(), e.getMessage());
        }
    }

    /**
     * 设置同一账户多会话
     */
    public void setupMultiPoint(Authentication authentication) {
        if (authentication instanceof JwtTokenAuthentication jwtTokenAuthentication) {
            val details = (AuthenticationDetails) jwtTokenAuthentication.getDetails();
            val app = details.getApp();
            if (StringUtils.isBlank(app) || !securityConfigProperties.getApp().containsKey(app)
                    || securityConfigProperties.getApp().get(app).isMultiPointEnabled()) {
                return;
            }

            try {
                val id = "login." + UCenterTokenManager.getAuthId(authentication);
                val ac = MD5.create().digestHex(jwtTokenAuthentication.getJwtToken().getAccessToken());
                val duration = securityConfigProperties.refreshTokenTimeout(details.getApp()).plusSeconds(10);
                AuthData authData = new AuthData().setToken(ac).setExpireTime(duration);
                tempStorageService.save(id, authData);
            }
            catch (Exception e) {
                log.warn("保存用户登录会话信息出错: {}\n{}", e.getMessage(), jwtTokenAuthentication.getJwtToken());
            }
        }
    }

    private void remove(String authId) {
        val duration = securityConfigProperties.getAccessTokenTimeout().plusMinutes(1);
        val data = new AuthData().setMessage("{uc.hadLogout}").setExpireTime(duration);
        try {
            tempStorageService.save(authId, data);
            log.trace("Session mark removed: {}", authId);
        }
        catch (Exception e) {
            log.error("Cannot mark the user logout：{} - {}", authId, e.getMessage());
        }
    }

    @Nonnull
    public static String getAuthId(Authentication authentication) {
        if (authentication instanceof JwtTokenAuthentication jwtAuth) {
            String provider = UserToken.DEFAULT_PROVIDER;
            String appId = UserToken.DEFAULT_APPID;
            if (jwtAuth.getDetails() instanceof AuthenticationDetails deviceAuthenticationDetails) {
                val meta = deviceAuthenticationDetails.getMeta();
                provider = meta.getOrDefault("PROVIDER", UserToken.DEFAULT_PROVIDER);
                appId = meta.getOrDefault("APPID", UserToken.DEFAULT_APPID);
            }
            return jwtAuth.getJwtToken().authId(String.format("%s@%s", provider, appId));
        }
        else {
            return JwtToken.authId(authentication.getPrincipal().toString(), "*", null);
        }
    }

    @Nonnull
    public static String getAuthId(String uid, String provider, String appId) {
        return JwtToken.authId(uid, String.format("%s@%s", provider, appId), null);
    }

}

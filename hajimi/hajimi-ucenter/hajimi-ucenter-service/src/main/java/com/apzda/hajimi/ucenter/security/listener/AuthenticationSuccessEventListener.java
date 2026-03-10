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
package com.apzda.hajimi.ucenter.security.listener;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.hajimi.ucenter.domain.entity.OauthEntity;
import com.apzda.hajimi.ucenter.domain.entity.OauthSessionEntity;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.security.token.UCenterTokenManager;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.event.LoginSuccessEvent;
import com.apzda.kalami.security.token.TokenManager;
import com.apzda.kalami.service.TempStorageService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationSuccessEventListener implements ApplicationListener<LoginSuccessEvent> {

    private final UserRepository userRepository;

    private final TokenManager tokenManager;

    private final TempStorageService tempStorageService;

    @Override
    public void onApplicationEvent(@Nonnull LoginSuccessEvent event) {
        log.trace("Authentication success event received");
        if (event.getAuthentication() instanceof JwtTokenAuthentication jwtAuth) {
            val authDetails = jwtAuth.getAuthDetails();
            if (authDetails == null) {
                return;
            }

            if (tokenManager instanceof UCenterTokenManager ucenterTokenManager) {
                ucenterTokenManager.setupMultiPoint(jwtAuth);
            }

            var meta = authDetails.getMeta();
            if (meta == null) {
                meta = new HashMap<>();
            }

            if (StringUtils.isNotBlank(meta.get("refresh"))) {
                // 刷新时不记录会话
                return;
            }

            val uid = Long.parseLong(jwtAuth.getUserDetails().getUsername());

            val provider = meta.getOrDefault("PROVIDER", UserToken.DEFAULT_PROVIDER);
            val appId = meta.getOrDefault("APPID", UserToken.DEFAULT_APPID);
            meta.remove("PROVIDER");
            meta.remove("APPID");

            val oauth = new OauthEntity();

            oauth.setAppId(appId);
            oauth.setProvider(provider);
            oauth.setUid(uid);
            oauth.setSpm(authDetails.getSpm());
            oauth.setDevice(authDetails.getDevice());
            oauth.setLoginTime(System.currentTimeMillis());
            oauth.setIp(authDetails.getRemoteAddress());
            oauth.setUserAgent(authDetails.getUserAgent());

            // 更新认证
            userRepository.updateOauthByUidAndProviderAndAppId(oauth, uid, provider, appId);

            // 写会话
            if (oauth.getId() != null) {
                val oauthSessionEntity = new OauthSessionEntity();
                BeanUtil.copyProperties(oauth, oauthSessionEntity, "id");
                oauthSessionEntity.setOauthId(oauth.getId());
                oauthSessionEntity.setAccessToken(authDetails.getAccessToken());
                oauthSessionEntity.setRefreshToken(authDetails.getRefreshToken());
                oauthSessionEntity.setExpiredAt(authDetails.getExpiredAt());
                if (!CollectionUtils.isEmpty(meta)) {
                    oauthSessionEntity.setExtra(meta);
                }
                userRepository.createOauthSession(oauthSessionEntity);
            }

            // 删除注销标记
            try {
                val authId = UCenterTokenManager.getAuthId(String.valueOf(uid), provider, appId);
                tempStorageService.remove(authId);
            }
            catch (Exception e) {
                log.warn("删除注销'{}({}@{})'标记失败 - {}", uid, provider, appId, e.getMessage());
            }
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return false;
    }

}

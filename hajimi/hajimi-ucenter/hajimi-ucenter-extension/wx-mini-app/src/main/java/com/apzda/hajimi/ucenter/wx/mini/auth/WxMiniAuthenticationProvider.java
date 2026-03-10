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
package com.apzda.hajimi.ucenter.wx.mini.auth;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.apzda.hajimi.ucenter.domain.entity.OauthEntity;
import com.apzda.hajimi.ucenter.domain.repository.OauthRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.event.OauthLoginEvent;
import com.apzda.hajimi.ucenter.service.dto.OauthDto;
import com.apzda.hajimi.ucenter.utils.UserUtils;
import com.apzda.hajimi.ucenter.wx.mini.auth.token.WxMiniCodeToken;
import com.apzda.hajimi.weixin.ma.WxMaProperties;
import com.apzda.kalami.data.error.ServiceNotAvailableError;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.security.authentication.AuthenticationDetails;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.error.AuthenticationError;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WxMiniAuthenticationProvider implements AuthenticationProvider {

    public static final String PROVIDER = "wx";

    private static final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy();

    private final WxMaService wxMaService;

    private final OauthRepository oauthRepository;

    private final UserRepository userRepository;

    private final WxMaProperties wxMaProperties;

    private final ApplicationEventPublisher publisher;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof WxMiniCodeToken phoneCodeToken) {
            val dto = phoneCodeToken.getDto();
            val details = (AuthenticationDetails) authentication.getDetails();
            val meta = (details).getMeta();
            val appId = meta.getOrDefault("APPID", wxMaProperties.defaultAppId());
            meta.put("PROVIDER", PROVIDER);
            meta.put("APPID", appId);

            if (StringUtils.isBlank(appId)) {
                throw new CommonBizException("小程序暂不可用[APPID为空]");
            }

            if (!wxMaService.switchover(appId)) {
                throw new CommonBizException("小程序暂不可用[小程序未配置]");
            }

            val oauthDto = new OauthDto();
            oauthDto.setSpm(dto.getSpm());
            oauthDto.setLoginTime(LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()));
            oauthDto.setRecCode(dto.getRecCode());
            oauthDto.setDevice(details.getDevice());
            oauthDto.setIp(details.getRemoteAddress());
            oauthDto.setUserAgent(details.getUserAgent());
            oauthDto.setProvider(PROVIDER);
            oauthDto.setAppId(appId);

            try {
                val userService = wxMaService.getUserService();
                val sessionInfo = userService.getSessionInfo(dto.getCode());

                if (StringUtils.isNotEmpty(dto.getPhoneCode())) {
                    try {
                        val phoneNumber = userService.getPhoneNumber(dto.getPhoneCode());
                        oauthDto.setPhone(phoneNumber.getPhoneNumber());
                    }
                    catch (Exception e) {
                        log.error("微信小程序获取手机号失败: {}", e.getMessage());
                    }
                }
                if (StringUtils.isNotBlank(oauthDto.getPhone())) {
                    oauthDto.setOpenid(String.format("%s-%s", sessionInfo.getOpenid(), oauthDto.getPhone()));
                }
                else {
                    oauthDto.setOpenid(sessionInfo.getOpenid());
                }
                oauthDto.setUnionid(sessionInfo.getUnionid());

                OauthEntity oauth = oauthRepository.createOauth(oauthDto);
                val user = userRepository.getById(oauth.getUid());

                if (user == null) {
                    throw new CommonBizException("用户不存在");
                }

                val userDetails = UserUtils.createUserDetails(user, List.of());
                val authResult = JwtTokenAuthentication.authenticated(userDetails, "", details);

                val context = securityContextHolderStrategy.createEmptyContext();
                context.setAuthentication(authResult);
                securityContextHolderStrategy.setContext(context);

                try {
                    //@formatter:off
                    val event = new OauthLoginEvent.Event()
                        .setProvider(PROVIDER)
                        .setAppId(appId)
                        .setOpenId(sessionInfo.getOpenid())
                        .setUnionId(sessionInfo.getUnionid())
                        .setUid(String.valueOf(oauth.getUid()))
                        .setOauthId(String.valueOf(oauth.getId()))
                        .setOrgId(dto.getShopId())
                        .setRecCode(dto.getRecCode())
                        .setPhone(oauthDto.getPhone())
                        .setSpm(dto.getSpm())
                        .setIp(details.getRemoteAddress())
                        .setDevice(details.getDevice())
                        .setUserAgent(details.getUserAgent());
                    //@formatter:on
                    publisher.publishEvent(new OauthLoginEvent(event, authResult));
                }
                catch (Exception e) {
                    log.error("发布微信用户登录成功事件失败: {}\n{}", e.getMessage(), oauth);
                }
                return authResult;
            }
            catch (Exception e) {
                log.error("微信小程序获取用户信息出错: {}", e.getMessage());
                throw new AuthenticationError(new ServiceNotAvailableError("微信小程序获取用户信息出错"), e);
            }
        }

        return null;
    }

    @Override
    public boolean supports(@Nonnull Class<?> authentication) {
        return authentication.equals(WxMiniCodeToken.class);
    }

}

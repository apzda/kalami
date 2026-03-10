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
package com.apzda.hajimi.ucenter.sms.auth;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.apzda.hajimi.sms.client.SmsService;
import com.apzda.hajimi.sms.client.dto.VerifyDTO;
import com.apzda.hajimi.sms.core.dto.Variable;
import com.apzda.hajimi.sms.error.SmsError;
import com.apzda.hajimi.ucenter.domain.entity.OauthEntity;
import com.apzda.hajimi.ucenter.domain.repository.OauthRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.event.OauthLoginEvent;
import com.apzda.hajimi.ucenter.service.dto.OauthDto;
import com.apzda.hajimi.ucenter.sms.auth.token.PhoneCodeToken;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.apzda.hajimi.ucenter.utils.UserUtils;
import com.apzda.kalami.data.error.ServiceNotAvailableError;
import com.apzda.kalami.security.authentication.AuthenticationDetails;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.error.AuthenticationError;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class PhoneCodeAuthenticationProvider implements AuthenticationProvider {

    public static final String PROVIDER = "phone";

    private static final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy();

    private final SmsService smsService;

    private final OauthRepository oauthRepository;

    private final UserRepository userRepository;

    private final ApplicationEventPublisher publisher;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof PhoneCodeToken phoneCodeToken) {
            val dto = phoneCodeToken.getDto();
            val phone = dto.getPhone();
            val code = dto.getCode();
            val req = new VerifyDTO();
            req.setPhone(phone);
            req.setVariables(List.of(new Variable("code", code, 0)));
            req.setTid("login");

            try {
                val verified = smsService.verify(req);
                if (!Boolean.TRUE.equals(verified.getData())) {
                    log.warn("短信验证失败: {} - {} - {}", phone, code, verified.getErrMsg());
                    throw new AuthenticationError(new SmsError(verified.getErrCode(), verified.getErrMsg()));
                }
            }
            catch (Exception e) {
                log.error("短信验证失败: {} - {} - {}", phone, code, e.getMessage());
                throw new AuthenticationError(new SmsError("短信验证失败,请稍后再试"));
            }

            val details = (AuthenticationDetails) authentication.getDetails();
            val meta = (details).getMeta();
            meta.put("PROVIDER", PROVIDER);
            meta.put("APPID", UserToken.DEFAULT_APPID);

            val oauthDto = new OauthDto();
            oauthDto.setOpenid(phone);
            oauthDto.setLoginTime(LocalDateTimeUtil.toEpochMilli(LocalDateTime.now()));
            oauthDto.setSpm(dto.getSpm());
            oauthDto.setRecCode(dto.getRecCode());
            oauthDto.setDevice(details.getDevice());
            oauthDto.setIp(details.getRemoteAddress());
            oauthDto.setUserAgent(details.getUserAgent());
            oauthDto.setProvider(PROVIDER);
            oauthDto.setAppId(UserToken.DEFAULT_APPID);
            oauthDto.setPhone(phone);

            try {
                OauthEntity oauth = oauthRepository.createOauth(oauthDto);
                val user = userRepository.getById(oauth.getUid());

                if (user == null) {
                    return null;
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
                        .setAppId(UserToken.DEFAULT_APPID)
                        .setOpenId(phone)
                        .setUnionId(null)
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
                    log.error("发布用户短信登录成功事件失败: {}\n{}", e.getMessage(), oauth);
                }

                return authResult;
            }
            catch (Exception e) {
                log.error("短信登录出错: {} - {}", phone, e.getMessage());
                throw new AuthenticationError(new ServiceNotAvailableError("短信登录出错"), e);
            }
        }

        return null;
    }

    @Override
    public boolean supports(@Nonnull Class<?> authentication) {
        return authentication.equals(PhoneCodeToken.class);
    }

}

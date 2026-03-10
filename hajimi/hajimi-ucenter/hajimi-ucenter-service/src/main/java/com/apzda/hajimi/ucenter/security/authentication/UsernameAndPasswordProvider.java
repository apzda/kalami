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
package com.apzda.hajimi.ucenter.security.authentication;

import com.apzda.hajimi.auditor.Audit;
import com.apzda.hajimi.auditor.AuditContextHolder;
import com.apzda.hajimi.captcha.aop.ValidateCaptcha;
import com.apzda.hajimi.ucenter.domain.entity.UserEntity;
import com.apzda.hajimi.ucenter.domain.repository.OauthRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.security.authentication.token.UsernameAndPasswordToken;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.apzda.hajimi.ucenter.utils.UserUtils;
import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.security.authentication.AuthenticationDetails;
import com.apzda.kalami.security.authentication.DeviceAuthenticationDetails;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class UsernameAndPasswordProvider implements AuthenticationProvider {

    private static final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
        .getContextHolderStrategy();

    private final UserRepository userRepository;

    private final OauthRepository oauthRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Audit(activity = "login", succTpl = "authenticated successfully", errorTpl = "authenticated failure: {}",
            args = { "#throwExp?.message" })
    @ValidateCaptcha
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.trace("Start Authenticate Authentication: {}", authentication);
        val username = authentication.getPrincipal();
        val password = authentication.getCredentials();
        if (password == null || username == null || StringUtils.isBlank(username.toString())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        AuditContextHolder.getContext().setNewValue(username.toString());

        if (authentication instanceof UsernameAndPasswordToken auth) {
            val authDetails = (AuthenticationDetails) auth.getDetails();
            val meta = authDetails.getMeta();
            val provider = meta.getOrDefault("PROVIDER", UserToken.DEFAULT_PROVIDER);
            val appId = meta.getOrDefault("APPID", UserToken.DEFAULT_APPID);
            val oauth = oauthRepository.getByProviderAndAppIdAndOpenId(provider, appId, username.toString());
            if (oauth == null) {
                throw new BadCredentialsException("Invalid username or password");
            }

            UserEntity user = userRepository.getById(oauth.getUid());
            if (user == null) {
                throw new BadCredentialsException("Invalid username or password");
            }

            if (passwordEncoder.matches(password.toString(), user.getPasswd())) {
                val userDetails = UserUtils.createUserDetails(user, List.of());

                val details = DeviceAuthenticationDetails.create(KalamiContextHolder.headers(),
                        KalamiContextHolder.getRemoteIp());

                val authResult = JwtTokenAuthentication.authenticated(userDetails, user.getPasswd(), details);

                val context = securityContextHolderStrategy.createEmptyContext();
                context.setAuthentication(authResult);
                securityContextHolderStrategy.setContext(context);

                return authResult;
            }

            AuditContextHolder.getContext().setUserId(String.valueOf(user.getId()));
        }

        throw new BadCredentialsException("Invalid username or password");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernameAndPasswordToken.class.isAssignableFrom(authentication);
    }

}

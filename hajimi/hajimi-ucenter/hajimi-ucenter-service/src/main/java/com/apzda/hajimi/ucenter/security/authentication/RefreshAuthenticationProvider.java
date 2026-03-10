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

import com.apzda.hajimi.ucenter.domain.entity.UserEntity;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.security.authentication.token.RefreshAuthenticationToken;
import com.apzda.hajimi.ucenter.security.token.UCenterTokenManager;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.apzda.hajimi.ucenter.utils.UserUtils;
import com.apzda.kalami.security.authentication.AuthenticationDetails;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.exception.TokenException;
import com.apzda.kalami.security.token.TokenManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshAuthenticationProvider implements AuthenticationProvider {

    private final TokenManager tokenManager;

    private final UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof RefreshAuthenticationToken auth) {
            tokenManager.verify(auth);
            val details = (AuthenticationDetails) auth.getDetails();
            val meta = details.getMeta();
            val provider = meta.getOrDefault("PROVIDER", UserToken.DEFAULT_PROVIDER);
            val appId = meta.getOrDefault("APPID", UserToken.DEFAULT_APPID);
            val token = auth.getToken();
            val uid = token.getUid();

            UserEntity entity = userRepository.findByProviderAndAppIdAndUserId(provider, appId, uid);

            if (entity == null) {
                throw TokenException.INVALID_TOKEN;
            }

            val userDetails = UserUtils.createUserDetails(entity, List.of());

            val newToken = tokenManager.refresh(token, userDetails);

            val authenticated = JwtTokenAuthentication.authenticated(userDetails, userDetails.getPassword(), details);

            authenticated.setJwtToken(newToken);

            if (tokenManager instanceof UCenterTokenManager ucenterTokenManager) {
                ucenterTokenManager.invalidAccessToken(token, details);
            }

            return authenticated;
        }

        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return RefreshAuthenticationToken.class.equals(authentication);
    }

}

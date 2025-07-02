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

package com.apzda.kalami.demo.user.controller.api;

import com.apzda.kalami.data.Response;
import com.apzda.kalami.security.authentication.JwtTokenAuthentication;
import com.apzda.kalami.security.token.JwtToken;
import com.apzda.kalami.security.token.TokenManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/20
 * @version 1.0.0
 */
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final TokenManager tokenManager;

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public Response<JwtToken> login() {
        val user = User.withUsername("1234567890123456789").accountLocked(true).password("123").build();

        val authenticated = JwtTokenAuthentication.authenticated(user, "");

        val jwtToken = tokenManager.create(authenticated);

        return Response.success(jwtToken);
    }

}

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
package com.apzda.hajimi.ucenter.wx.mini.controller;

import com.apzda.hajimi.ucenter.wx.mini.auth.token.WxMiniCodeToken;
import com.apzda.hajimi.ucenter.wx.mini.dto.LoginDto;
import com.apzda.kalami.security.authentication.AuthenticationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/app/uc/wx/mini")
@RequiredArgsConstructor
public class WxMiniAppController {

    /**
     * 微信小程序登录
     */
    @PostMapping("/login")
    public void login(@RequestBody @Validated LoginDto dto, HttpServletRequest request, HttpServletResponse response) {
        val authentication = new WxMiniCodeToken(dto, request);

        // 委派给AuthenticationManager
        AuthenticationUtil.authenticate(authentication, request, response);
    }

}

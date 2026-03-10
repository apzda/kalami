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
package com.apzda.hajimi.ucenter.sms.controller;

import cn.hutool.core.util.NumberUtil;
import com.apzda.hajimi.sms.client.SmsService;
import com.apzda.hajimi.sms.client.dto.SendSmsDTO;
import com.apzda.hajimi.sms.core.dto.Variable;
import com.apzda.hajimi.ucenter.sms.auth.token.PhoneCodeToken;
import com.apzda.hajimi.ucenter.sms.dto.LoginDto;
import com.apzda.kalami.data.MessageType;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.security.authentication.AuthenticationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/app/uc/phone")
@RequiredArgsConstructor
public class PhoneCodeLoginController {

    private final SmsService smsService;

    /**
     * 获取登录验证码
     */
    @PostMapping("/sendSms/{phone}")
    public Response<?> sendSms(@PathVariable String phone) {
        try {
            val codes = NumberUtil.generateRandomNumber(1000, 9999, 1);
            val dto = new SendSmsDTO();
            dto.setTid("login");
            dto.setPhones(List.of(phone));
            dto.setVariables(List.of(new Variable("code", String.valueOf(codes[0]), 0)));
            dto.setSync(false);

            return smsService.send(dto);
        }
        catch (Exception e) {
            log.error("发送短信失败: {} - {}", phone, e.getMessage());
        }

        return Response.error("短信发送失败,请稍后再试").withType(MessageType.ALERT);
    }

    /**
     * 短信验证码登录
     */
    @PostMapping("/login")
    public void login(@RequestBody @Validated LoginDto dto, HttpServletRequest request, HttpServletResponse response) {
        val authentication = new PhoneCodeToken(dto, request);

        // 委派给AuthenticationManager
        AuthenticationUtil.authenticate(authentication, request, response);
    }

}

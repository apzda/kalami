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
package com.apzda.hajimi.captcha.controller.internal;

import cn.hutool.core.date.DateUtil;
import com.apzda.hajimi.captcha.Captcha;
import com.apzda.hajimi.captcha.client.CaptchaService;
import com.apzda.hajimi.captcha.client.dto.CheckDTO;
import com.apzda.hajimi.captcha.config.CaptchaConfigProperties;
import com.apzda.hajimi.captcha.config.CaptchaServiceConfiguration;
import com.apzda.hajimi.captcha.storage.CaptchaStorage;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.i18n.I18n;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RestController("mashupCaptchaServiceController")
@RequiredArgsConstructor
@Primary
public class CaptchaServiceController implements CaptchaService {

    private final CaptchaServiceConfiguration captchaConfig;

    private final CaptchaConfigProperties properties;

    private final CaptchaStorage captchaStorage;

    @Override
    public Response<Boolean> check(CheckDTO request) {
        if (Boolean.TRUE.equals(properties.getProps().get("test-mode"))) {
            return Response.success(true);
        }
        val uuid = request.getUuid();
        val id = request.getId();
        val captcha = new Captcha();
        captcha.setId(id);
        try {
            val ca = captchaStorage.load("v_" + uuid, captcha);
            if (ca == null) {
                throw new IllegalStateException("Cannot load captcha from storage");
            }
            val now = DateUtil.currentSeconds();
            if (now > ca.getExpireTime()) {
                return Response.error(2, I18n.t("captcha.expired"));
            }
            else if (!Captcha.VERIFIED.equals(ca.getCode())) {
                return Response.error(1, I18n.t("captcha.invalid"));
            }
            return Response.success(true);
        }
        catch (Exception e) {
            log.error("Cannot load captcha(uuid: {}, id: {}) - {}", uuid, id, e.getMessage());
            return Response.error(1, I18n.t("captcha.invalid"));
        }
        finally {
            // 为了安全，只要做了check，就删除.
            try {
                captchaStorage.remove(uuid, captcha);
            }
            catch (Exception ignored) {
            }
            try {
                captchaStorage.remove("v_" + uuid, captcha);
            }
            catch (Exception ignored) {
            }
        }
    }

}

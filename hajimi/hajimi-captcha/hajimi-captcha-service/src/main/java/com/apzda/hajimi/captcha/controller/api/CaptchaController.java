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
package com.apzda.hajimi.captcha.controller.api;

import cn.hutool.core.date.DateUtil;
import com.apzda.hajimi.captcha.Captcha;
import com.apzda.hajimi.captcha.ValidateStatus;
import com.apzda.hajimi.captcha.client.dto.ValidateReq;
import com.apzda.hajimi.captcha.client.vo.CreateCaptchaRes;
import com.apzda.hajimi.captcha.client.vo.ValidateRes;
import com.apzda.hajimi.captcha.config.CaptchaConfigProperties;
import com.apzda.hajimi.captcha.config.CaptchaServiceConfiguration;
import com.apzda.hajimi.captcha.error.CaptchaError;
import com.apzda.hajimi.captcha.storage.CaptchaStorage;
import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.i18n.I18n;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static com.apzda.kalami.context.KalamiContextHolder.header;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
@RestController("sysCaptchaController")
@RequestMapping("/mashup/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaServiceConfiguration captchaConfig;

    private final CaptchaConfigProperties properties;

    private final CaptchaStorage captchaStorage;

    /**
     * 创建验证码
     */
    @GetMapping("/create")
    public Response<CreateCaptchaRes> create(@RequestParam(required = false, name = "width") Integer width,
            @RequestParam(required = false, name = "height") Integer height) {
        val builder = new CreateCaptchaRes();
        val remoteIp = KalamiContextHolder.getRemoteIp();
        int count = captchaStorage.getIpCount(remoteIp);
        if (count > properties.getMaxCount()) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS);
        }
        val captchaProvider = captchaConfig.getCaptchaProvider();
        if (captchaProvider == null) {
            return Response.error(500, I18n.t("captcha.provider.404"));
        }
        else {
            val uuid = header("X-CAPTCHA-UUID", header("UUID"));
            if (StringUtils.isBlank(uuid)) {
                return Response.error(1, I18n.t("captcha.uuid.missing"));
            }
            width = Optional.ofNullable(width).orElse(0);
            height = Optional.ofNullable(height).orElse(0);
            if (width <= 0) {
                width = properties.getWidth();
            }
            if (height <= 0) {
                height = properties.getHeight();
            }
            try {
                val captcha = captchaProvider.create(uuid, width, height, properties.getTimeout());
                builder.setType(captchaProvider.getId());
                builder.setId(captcha.getId());
                builder.setCaptcha(captcha.getCode());
                builder.setExpireTime(captcha.getExpireTime());
            }
            catch (Exception e) {
                log.warn("Cannot create captcha - {}", e.getMessage());
                return Response.error(500, I18n.t("captcha.provider.500"));
            }
        }
        return Response.success(builder);
    }

    /**
     * 校验验证码
     */
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Response<ValidateRes> validate(@RequestBody ValidateReq request) {
        val response = new ValidateRes();
        val uuid = header("X-CAPTCHA-UUID", header("UUID"));
        if (StringUtils.isBlank(uuid)) {
            return Response.error(1, I18n.t("captcha.uuid.missing"));
        }
        val id = header("X-CAPTCHA-ID", request.getId());
        if (StringUtils.isBlank(id)) {
            return Response.error(1, I18n.t("captcha.id.missing"));
        }
        val code = request.getCode();
        if (StringUtils.isBlank(code)) {
            return Response.error(1, I18n.t("captcha.code.missing"));
        }
        val captchaProvider = captchaConfig.getCaptchaProvider();
        val removeOnInvalid = properties.isRemoveOnInvalid();
        if (captchaProvider == null) {
            return Response.error(1, I18n.t("captcha.invalid"));
        }

        try {
            val validate = captchaProvider.validate(uuid, id, code, removeOnInvalid);
            if (validate == ValidateStatus.OK) {
                val captcha = new Captcha();
                captcha.setId(id);
                captcha.setCode(Captcha.VERIFIED);
                captcha.setExpireTime(DateUtil.currentSeconds() + properties.getExpired().toSeconds());
                captchaStorage.save("v_" + uuid, captcha);
                return Response.success(response);
            }
            else if (validate == ValidateStatus.EXPIRED) {
                response.setReload(true);
                return Response.error(new CaptchaError(2, I18n.t("captcha.expired")), response);
            }
            else if (removeOnInvalid || captchaStorage.getErrorCount(uuid, id) > properties.getMaxTryCount()) {
                response.setReload(true);
            }

        }
        catch (Exception e) {
            log.warn("Error occurred while validate captcha(uuid: {}, id: {}) - {}", uuid, id, e.getMessage());
        }

        return Response.error(new CaptchaError(I18n.t("captcha.invalid")), response);
    }

}

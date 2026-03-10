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
package com.apzda.hajimi.captcha.client;

import com.apzda.hajimi.captcha.CaptchaException;
import com.apzda.hajimi.captcha.client.dto.CheckDTO;
import com.apzda.hajimi.captcha.error.CaptchaError;
import com.apzda.hajimi.captcha.error.CaptchaExpired;
import com.apzda.hajimi.captcha.error.MissingCaptcha;
import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.i18n.I18n;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
//@formatter:off
@FeignClient(
    name = "${kalami.cloud.feign.service.mashup.name:mashupSvc}",
    url = "${kalami.cloud.feign.service.mashup.url:}",
    contextId = "kalami.cloud.feign.service.CaptchaService",
    primary = false
)
//@formatter:on
public interface CaptchaService {

    Logger log = LoggerFactory.getLogger(CaptchaService.class);

    default void validate(String captchaId) {

        val uuid = StringUtils.defaultIfBlank(KalamiContextHolder.header("X-CAPTCHA-UUID"),
                KalamiContextHolder.header("UUID"));
        val id = StringUtils.defaultIfBlank(captchaId, KalamiContextHolder.header("X-CAPTCHA-ID"));

        validate(uuid, id);
    }

    default void validate(String uuid, String id) {
        if (StringUtils.isBlank(uuid) || StringUtils.isBlank(id)) {
            throw new CaptchaException(new MissingCaptcha());
        }
        try {
            val dto = new CheckDTO();
            val validate = check(dto.setId(id).setUuid(uuid));
            if (validate == null) {
                log.warn("Captcha(uuid:{}, id:{}) is error: no response", uuid, id);
                throw new CaptchaException(new CaptchaError(I18n.t("captcha.invalid")));
            }
            else if (validate.getErrCode() == 1) {
                log.warn("Captcha(uuid:{}, id:{}) is invalid: {}", uuid, id, validate.getErrMsg());
                throw new CaptchaException(new CaptchaError(validate.getErrMsg()));
            }
            else if (validate.getErrCode() == 2) {
                log.warn("Captcha(uuid:{}, id:{}) is expired: {}", uuid, id, validate.getErrMsg());
                throw new CaptchaException(new CaptchaExpired(validate.getErrMsg()));
            }
            else if (validate.getErrCode() != 0 || !Boolean.TRUE.equals(validate.getData())) {
                throw new CaptchaException(new CaptchaError(validate.getErrMsg()));
            }
        }
        catch (CaptchaException ce) {
            throw ce;
        }
        catch (Exception e) {
            throw new CaptchaException(new CaptchaError(e.getMessage()));
        }
    }

    /**
     * 确认验证通过
     */
    @PostMapping(value = "/_/mashupSvc/captcha/check", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<Boolean> check(CheckDTO dto);

}

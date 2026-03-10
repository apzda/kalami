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
package com.apzda.hajimi.captcha.aop;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.hajimi.captcha.CaptchaException;
import com.apzda.hajimi.captcha.ICaptchaData;
import com.apzda.hajimi.captcha.client.CaptchaService;
import com.apzda.hajimi.captcha.client.dto.CheckDTO;
import com.apzda.hajimi.captcha.error.CaptchaError;
import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.i18n.I18n;
import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Aspect
@Component
@Slf4j
public class VerificationInterceptor {

    private final Lazy<CaptchaService> captchaServiceLoader;

    public VerificationInterceptor(ApplicationContext applicationContext) {
        this.captchaServiceLoader = Lazy.of(() -> applicationContext.getBean(CaptchaService.class));
    }

    @Before("@annotation(com.apzda.hajimi.captcha.aop.ValidateCaptcha)")
    public void interceptor(@Nonnull JoinPoint joinPoint) {
        val args = joinPoint.getArgs();
        val captchaData = new CaptchaData();
        if (args.length > 0 && !BeanUtils.isSimpleProperty(args[0].getClass())) {
            BeanUtil.copyProperties(args[0], captchaData);
            log.trace("Retrieve Captcha data from args[0]: {}", captchaData);
        }

        if (StringUtils.isBlank(captchaData.getCaptchaId())) {
            val request = KalamiContextHolder.getRequest();
            if (request.isPresent()) {
                val uuid = StringUtils.defaultIfBlank(KalamiContextHolder.header("X-CAPTCHA-UUID"),
                        KalamiContextHolder.header("UUID"));
                val id = KalamiContextHolder.header("X-CAPTCHA-ID");
                captchaData.setCaptchaId(id);
                captchaData.setCaptchaUuid(uuid);
                log.trace("Retrieve Captcha data from header: {}", captchaData);
            }
        }

        if (StringUtils.isNotBlank(captchaData.getCaptchaId())) {
            val uuid = captchaData.getCaptchaUuid();
            val id = captchaData.getCaptchaId();
            log.debug("Start to validate captcha: uuid({}), id({}))", uuid, id);
            val req = new CheckDTO().setUuid(uuid).setId(id);
            val captchaService = captchaServiceLoader.get();
            captchaService.validate(uuid, id);
            return;
        }
        log.debug("Captcha not supported!");
        throw new CaptchaException(new CaptchaError(I18n.t("captcha.not.support")));
    }

    @Data
    static class CaptchaData implements ICaptchaData {

        private String captchaUuid;

        private String captchaId;

    }

}

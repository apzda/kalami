/*
 * Copyright 2023-2026 the original author or authors.
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
package com.apzda.hajimi.sms.aop;

import com.apzda.hajimi.sms.ISmsData;
import com.apzda.hajimi.sms.client.SmsService;
import com.apzda.hajimi.sms.client.dto.VerifyDTO;
import com.apzda.hajimi.sms.core.dto.Variable;
import com.apzda.hajimi.sms.error.SmsInvalidException;
import com.apzda.kalami.context.KalamiContextHolder;
import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
@Aspect
@Component
public class VerificationAdvise {

    private final Lazy<SmsService> smsServiceLoader;

    public VerificationAdvise(ApplicationContext applicationContext) {
        this.smsServiceLoader = Lazy.of(() -> applicationContext.getBean(SmsService.class));
    }

    @Before("@annotation(com.apzda.hajimi.sms.aop.ValidateSms)")
    public void interceptor(@Nonnull JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ValidateSms verificationAnnotation = method.getAnnotation(ValidateSms.class);
        val tid = verificationAnnotation.tid();
        if (StringUtils.isBlank(tid)) {
            throw new IllegalArgumentException("tid is blank");
        }
        val name = verificationAnnotation.name();
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name is blank");
        }
        val args = joinPoint.getArgs();
        val smsData = new SmsData();

        if (args.length > 0 && !BeanUtils.isSimpleProperty(args[0].getClass())) {
            BeanUtils.copyProperties(args[0], smsData);
            log.trace("Retrieve Sms data from args[0]: {}", smsData);
        }

        if (StringUtils.isBlank(smsData.getPhone())) {
            val request = KalamiContextHolder.getRequest();
            if (request.isPresent()) {
                val phone = KalamiContextHolder.header("X-SMS-PHONE");
                val code = KalamiContextHolder.header("X-SMS-CODE");
                smsData.setPhone(phone);
                smsData.setCode(code);
                log.trace("Retrieve Sms data from header: {}", smsData);
            }
        }

        if (smsData.isValid()) {
            val phone = smsData.getPhone();
            log.debug("Start to validate Sms: {}", smsData);
            val variable = new Variable().setName(name).setValue(smsData.getCode());

            val req = new VerifyDTO().setPhone(phone).setTid(tid);
            req.getVariables().add(variable);
            val smsService = smsServiceLoader.get();
            val validate = smsService.verify(req);
            if (validate == null) {
                log.warn("Sms({}) is error: no response", smsData);
                throw new SmsInvalidException();
            }
            else if (validate.getErrCode() != 0 || !Boolean.TRUE.equals(validate.getData())) {
                log.warn("Sms({}) is invalid: {}", smsData, validate.getErrMsg());
                throw new SmsInvalidException(validate.getErrCode(), validate.getErrMsg());
            }
            return;
        }
        log.debug("Sms({}) is error: missing data", smsData);
        throw new SmsInvalidException();
    }

    @Data
    static class SmsData implements ISmsData {

        private String phone;

        private String code;

        public boolean isValid() {
            return StringUtils.isNoneBlank(phone) && StringUtils.isNotBlank(code);
        }

    }

}

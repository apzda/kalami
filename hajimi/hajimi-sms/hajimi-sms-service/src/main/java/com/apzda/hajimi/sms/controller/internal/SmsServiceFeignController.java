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
package com.apzda.hajimi.sms.controller.internal;

import com.apzda.hajimi.sms.client.SmsService;
import com.apzda.hajimi.sms.client.dto.SendSmsDTO;
import com.apzda.hajimi.sms.client.dto.VerifyDTO;
import com.apzda.hajimi.sms.core.SmsSender;
import com.apzda.hajimi.sms.core.SmsTemplate;
import com.apzda.hajimi.sms.core.config.TemplateProperties;
import com.apzda.hajimi.sms.core.dto.Variable;
import com.apzda.hajimi.sms.core.exception.TooManySmsException;
import com.apzda.hajimi.sms.service.config.SmsConfigProperties;
import com.apzda.hajimi.sms.service.config.SmsServiceConfiguration;
import com.apzda.hajimi.sms.service.domain.entity.SmsLog;
import com.apzda.hajimi.sms.service.domain.repository.SmsLogRepository;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.i18n.I18n;
import com.apzda.kalami.service.CounterService;
import com.apzda.kalami.service.TempStorageService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController("mashupSmsServiceFeignController")
@RequiredArgsConstructor
@Primary
public class SmsServiceFeignController implements SmsService, InitializingBean {

    private final SmsLogRepository smsLogRepository;

    private final SmsServiceConfiguration serviceConfig;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final TempStorageService storage;

    private final CounterService counter;

    private final SmsConfigProperties smsConfigProperties;

    private SmsSender<?> smsSender;

    private String vendor;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.smsSender = serviceConfig.getSmsSender();
        vendor = serviceConfig.getSmsProvider().getId();
    }

    @Override
    public Response<Boolean> verify(@Validated @Nonnull VerifyDTO request) {

        val templateId = request.getTid();
        val phone = request.getPhone();
        val variables = request.getVariables();

        val template = getSmsTemplate(templateId);
        if (template == null) {
            return responseNotFound(templateId);
        }

        val sms = template.create(phone, variables);
        sms.setTestMode(smsConfigProperties.isTestMode());

        if (template.verify(sms, storage)) {
            return Response.success(true);
        }

        return Response.error(3, I18n.t("sms.invalid"));
    }

    @Override
    @Nonnull
    public Response<Integer> send(@Validated @Nonnull SendSmsDTO request) {
        val phones = request.getPhones();
        val variables = request.getVariables();
        val templateId = request.getTid();
        var sync = request.isSync();

        if (phones.size() > 1) {
            sync = false;
        }

        val template = getSmsTemplate(templateId);
        if (template == null) {
            return responseNotFound(templateId);
        }

        for (String phone : phones) {
            try {
                checkLimit(phone, templateId, template.getProperties());
                val sms = template.create(phone, variables);
                sms.setSync(sync);
                sms.setVendor(vendor);
                sms.setTestMode(smsConfigProperties.isTestMode());
                template.beforeSend(sms, storage);
                val smsLog = new SmsLog();
                smsLog.setPhone(sms.getPhone());
                smsLog.setTid(sms.getTid());
                smsLog.setVendor(vendor);
                smsLog.setIntervals(sms.getIntervals());
                smsLog.setContent(sms.getContent());
                smsLog.setParams(Variable.toJsonStr(variables));
                try {
                    if (smsLogRepository.save(smsLog)) {
                        sms.setSmsLogId(smsLog.getId());
                    }
                    else {
                        log.warn("Sms log save failed");
                    }
                }
                catch (Exception e) {
                    log.warn("Sms log save failed：{}", e.getMessage());
                }
                smsSender.send(sms, applicationEventPublisher);
                template.onSent(sms, storage);
            }
            catch (Exception e) {
                log.warn("Cannot send sms({},{},{}) - {}", phone, templateId, variables, e.getMessage());
                return Response.error(500, I18n.t("sms.send.failed"));
            }
        }

        return Response.success((int) template.getProperties().getInterval().toSeconds());
    }

    private void checkLimit(String phone, String tid, TemplateProperties properties) {
        val ac = counter.count("sms.ld." + phone, Duration.ofDays(1).toSeconds());
        val pac = serviceConfig.getProperties().getMaxCount();
        if (ac > pac) {
            throw new TooManySmsException(String.format("%s had send %d sms today, limit is %d", phone, ac, pac));
        }
        val dc = counter.count("sms.ld." + tid + "." + phone, Duration.ofDays(1).toSeconds());
        val pdc = properties.theCountD();
        if (dc > pdc) {
            throw new TooManySmsException(
                    String.format("%s had send %d sms(%s) today, limit is %d", phone, dc, tid, pdc));
        }
        val hc = counter.count("sms.lh." + tid + "." + phone, Duration.ofHours(1).toSeconds());
        val phc = properties.theCountH();
        if (hc > phc) {
            throw new TooManySmsException(
                    String.format("%s had send %d sms(%s)/hour, limit is %d", phone, hc, tid, phc));
        }
        val mc = counter.count("sms.lm." + tid + "." + phone, 60);
        val pmc = properties.theCountH();
        if (mc > pmc) {
            throw new TooManySmsException(
                    String.format("%s had send %d sms(%s)/minute, limit is %d", phone, mc, tid, pmc));
        }
    }

    private SmsTemplate getSmsTemplate(String templateId) {
        val properties = serviceConfig.getProperties();
        val templates = properties.getTemplates();
        val providerProperties = serviceConfig.getProviderProperties();
        val pp = providerProperties.get(vendor);
        val tpl = pp.templates(templates);
        return tpl.get(templateId);
    }

    @Nonnull
    private <T> Response<T> responseNotFound(String templateId) {
        log.warn("Sms Template({}) is not available", templateId);
        return Response.error(1, I18n.t("sms.template.not.found", new String[] { templateId }));
    }

}

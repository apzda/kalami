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
package com.apzda.hajimi.sms.service.listener;

import cn.hutool.core.date.DateUtil;
import com.apzda.hajimi.sms.core.event.SmsEvent;
import com.apzda.hajimi.sms.service.domain.SmsStatus;
import com.apzda.hajimi.sms.service.domain.repository.SmsLogRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationListener;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
@RequiredArgsConstructor
public class SmsEventListener implements ApplicationListener<SmsEvent> {

    private final ObjectProvider<SmsLogRepository> smsLogRepository;

    @Override
    public void onApplicationEvent(@Nonnull SmsEvent event) {
        val source = event.getSms();
        val repository = smsLogRepository.getIfAvailable();
        if (repository == null) {
            log.info("SMS send: {}", source);
            return;
        }

        val smsLogId = source.getSmsLogId();
        if (smsLogId != null) {
            val sms = repository.findById(smsLogId);
            if (sms != null) {
                sms.setSentTime(DateUtil.currentSeconds());
                sms.setStatus(event.isSuccess() ? SmsStatus.SENT : SmsStatus.FAILED);
                val exception = event.getException();
                if (exception != null) {
                    sms.setError(exception.getMessage());
                }
                repository.save(sms);
            }
        }
    }

}

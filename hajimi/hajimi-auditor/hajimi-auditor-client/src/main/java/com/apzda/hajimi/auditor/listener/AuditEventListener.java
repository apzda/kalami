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
package com.apzda.hajimi.auditor.listener;

import com.apzda.hajimi.auditor.logging.AuditLoggerFactory;
import com.apzda.kalami.event.AuditEvent;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener implements ApplicationListener<AuditEvent> {

    private final AuditLoggerFactory loggerFactory;

    @Override
    public boolean supportsAsyncExecution() {
        return false;
    }

    @Override
    public void onApplicationEvent(@Nonnull AuditEvent event) {
        log.trace("Received AuditEvent {}", event);

        val audit = event.getAuditLog();
        val timestamp = event.getTimestamp();

        val activity = StringUtils.defaultIfBlank(audit.getActivity(), "op");
        val logger = loggerFactory.create(activity);
        logger.id(audit.getId());
        logger.target(audit.getTarget());

        if (StringUtils.isNotBlank(audit.getUserId())) {
            logger.userId(audit.getUserId());
        }
        if (StringUtils.isNotBlank(audit.getTenantId())) {
            logger.tenantId(audit.getTenantId());
        }

        logger.template(Boolean.TRUE.equals(audit.getTemplate()));
        logger.timestamp(timestamp);
        if (StringUtils.isNotBlank(audit.getRunas())) {
            logger.runas(audit.getRunas());
        }
        logger.level(audit.getLevel());

        if (StringUtils.isNotBlank(audit.getIp())) {
            logger.ip(audit.getIp());
        }
        if (StringUtils.isNotBlank(audit.getDevice())) {
            logger.device(audit.getDevice());
        }
        logger.message(audit.getMessage());
        List<String> args = audit.getArgs();

        if (!CollectionUtils.isEmpty(args)) {
            for (String arg : args) {
                logger.arg(arg);
            }
        }
        logger.replace(audit.getOldValue(), audit.getNewValue());

        logger.log();
    }

}

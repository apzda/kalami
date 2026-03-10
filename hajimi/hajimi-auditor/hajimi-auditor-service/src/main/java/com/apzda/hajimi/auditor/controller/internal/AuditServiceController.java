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
package com.apzda.hajimi.auditor.controller.internal;

import com.apzda.hajimi.auditor.client.AuditService;
import com.apzda.hajimi.auditor.domain.entity.AuditEntity;
import com.apzda.hajimi.auditor.domain.repository.AuditLogRepository;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.domain.AuditLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController("mashupAuditServiceController")
@RequiredArgsConstructor
@Primary
public class AuditServiceController implements AuditService {

    private final AuditLogRepository auditLogRepository;

    private final ObjectMapper objectMapper;

    @Override
    public Response<Boolean> log(@Nonnull AuditLog request) {
        val entity = new AuditEntity();
        entity.setTenantId(StringUtils.defaultIfBlank(request.getTenantId(), "0"));
        entity.setUserId(request.getUserId());
        entity.setTarget(StringUtils.defaultIfBlank(request.getTarget(), ""));
        entity.setLogTime(request.getTimestamp());
        entity.setActivity(request.getActivity());
        entity.setIp(request.getIp());
        entity.setMessage(request.getMessage());
        entity.setTemplate(request.getTemplate());
        entity.setRunas(request.getRunas());
        entity.setDevice(request.getDevice());
        entity.setLevel(StringUtils.defaultIfBlank(request.getLevel(), "info"));
        entity.setArgs(request.getArgs());
        if (request.getNewValue() != null) {
            try {
                entity.setNewValue(objectMapper.writeValueAsString(request.getNewValue()));
            }
            catch (Exception e) {
                log.warn("Can't serialize new value: {}", e.getMessage());
            }
        }
        if (request.getOldValue() != null) {
            try {
                entity.setOldValue(objectMapper.writeValueAsString(request.getOldValue()));
            }
            catch (JsonProcessingException e) {
                log.warn("Can't serialize old value: {}", e.getMessage());
            }
        }

        if (!auditLogRepository.save(entity)) {
            log.error("Cannot save audit log: {}", entity);

            return Response.error(503, "Cannot save audit log");
        }

        return Response.success(true);
    }

}

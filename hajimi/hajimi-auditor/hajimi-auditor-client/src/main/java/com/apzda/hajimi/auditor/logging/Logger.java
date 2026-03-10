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
package com.apzda.hajimi.auditor.logging;

import com.apzda.hajimi.auditor.client.AuditService;
import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.data.domain.AuditLog;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUserProvider;
import com.apzda.kalami.utils.SanitizeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
public class Logger {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("audit");

    private final AuditService auditService;

    private final ObjectMapper objectMapper;

    private final AuditLog auditLog;

    private final ObservationRegistry observationRegistry;

    public Logger(@NonNull String activity, AuditService auditService, ObjectMapper objectMapper,
            ObservationRegistry observationRegistry) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.observationRegistry = observationRegistry;
        this.auditLog = new AuditLog();
        val currentUser = CurrentUserProvider.getCurrentUser();
        val userId = Optional.ofNullable(currentUser.getUid()).orElse("0");
        val runAs = currentUser.getRunAs();
        val device = currentUser.getDevice();
        val tenantId = TenantManager.tenantId("0");
        var ip = KalamiContextHolder.getRemoteIp();

        this.auditLog.setActivity(activity)
            .setUserId(userId)
            .setTenantId(tenantId)
            .setIp(ip)
            .setLevel("info")
            .setTimestamp(System.currentTimeMillis());
        if (runAs != null) {
            this.auditLog.setRunas(runAs);
        }
        if (device != null) {
            this.auditLog.setDevice(device);
        }
    }

    public Logger id(String id) {
        this.auditLog.setId(id);
        return this;
    }

    public Logger userId(String userId) {
        if (userId != null) {
            this.auditLog.setUserId(userId);
        }
        else {
            this.auditLog.setUserId(CurrentUserProvider.getCurrentUser().getUid());
        }
        return this;
    }

    public Logger target(String target) {
        this.auditLog.setTarget(target);
        return this;
    }

    public Logger tenantId(String tenantId) {
        this.auditLog.setTenantId(Objects.requireNonNullElseGet(tenantId, () -> TenantManager.tenantId("0")));
        return this;
    }

    public Logger timestamp(long timestamp) {
        this.auditLog.setTimestamp(timestamp);
        return this;
    }

    public Logger runas(String runas) {
        if (runas == null) {
            runas = CurrentUserProvider.getCurrentUser().getRunAs();
        }
        if (StringUtils.isNotBlank(runas)) {
            this.auditLog.setRunas(runas);
        }
        return this;
    }

    public Logger level(String level) {
        this.auditLog.setLevel(Objects.requireNonNullElse(level, "info"));
        return this;
    }

    public Logger ip(String ip) {
        this.auditLog.setIp(StringUtils.defaultIfBlank(ip, KalamiContextHolder.getRemoteIp()));
        return this;
    }

    public Logger device(String device) {
        val d = StringUtils.defaultIfBlank(device, CurrentUserProvider.getCurrentUser().getDevice());
        if (d != null) {
            this.auditLog.setDevice(d);
        }
        return this;
    }

    public Logger message(String message) {
        if (message != null) {
            this.auditLog.setMessage(message);
        }
        return this;
    }

    public Logger replace(Object oldVal, Object newVal) {
        return oldValue(oldVal).newValue(newVal);
    }

    public Logger oldValue(Object oldVal) {
        if (oldVal != null) {
            this.auditLog.setOldValue(oldVal);
        }
        return this;
    }

    public Logger newValue(Object newVal) {
        if (newVal != null) {
            this.auditLog.setNewValue(newVal);
        }
        return this;
    }

    public Logger arg(String arg) {
        this.auditLog.getArgs().add(arg);
        return this;
    }

    public Logger template(boolean template) {
        this.auditLog.setTemplate(template);
        return this;
    }

    public void log() {
        log(this.auditLog);
    }

    public void log(@Nonnull AuditLog auditLog) {
        val context = KalamiContextHolder.getContext();
        val securityContext = SecurityContextHolder.getContext();

        Observation observation;
        if (observationRegistry != null) {
            observation = Observation.createNotStarted("audit log", observationRegistry);
        }
        else {
            observation = null;
        }

        CompletableFuture.runAsync(() -> {
            try {
                context.restore();
                SecurityContextHolder.setContext(securityContext);

                if (observation == null) {
                    log(this.auditService, this.objectMapper, auditLog);
                    return;
                }

                observation.observe(() -> {
                    SecurityContextHolder.setContext(securityContext);
                    context.restore();
                    try {
                        log(this.auditService, this.objectMapper, auditLog);
                    }
                    finally {
                        KalamiContextHolder.clear();
                        SecurityContextHolder.setContext(securityContext);
                    }
                });
            }
            finally {
                KalamiContextHolder.clear();
                SecurityContextHolder.setContext(securityContext);
            }
        });

    }

    static void log(AuditService auditService, ObjectMapper objectMapper, AuditLog auditLog) {
        try {
            try {
                auditLog.setNewValue(SanitizeUtils.sanitize(auditLog.getNewValue()));
            }
            catch (Exception e) {
                log.warn("Cannot sanitize new value: {}", e.getMessage());
            }
            try {
                auditLog.setOldValue(SanitizeUtils.sanitize(auditLog.getOldValue()));
            }
            catch (Exception e) {
                log.warn("Cannot sanitize old value: {}", e.getMessage());
            }

            val str = objectMapper.writeValueAsString(auditLog);
            logger.info("{}", str);

            if (auditService != null) {
                val rest = auditService.log(auditLog);
                if (rest == null) {
                    logAuditWarn(str, "Cannot save audit log");
                }
                else if (StringUtils.isNotBlank(rest.getErrMsg())) {
                    logAuditWarn(str, rest.getErrMsg());
                }
            }
        }
        catch (Exception e) {
            logAuditWarn(auditLog, e.getMessage());
        }
    }

    private static void logAuditWarn(Object arg, String message) {
        log.warn("Audit log failed: {} - {}", arg, message);
    }

}

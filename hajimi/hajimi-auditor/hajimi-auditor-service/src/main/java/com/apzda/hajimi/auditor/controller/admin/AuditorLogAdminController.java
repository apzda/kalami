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
package com.apzda.hajimi.auditor.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.hajimi.auditor.data.dto.AuditLogQuery;
import com.apzda.hajimi.auditor.data.vo.AuditLogVo;
import com.apzda.hajimi.auditor.domain.repository.AuditLogRepository;
import com.apzda.kalami.annotation.Dictionary;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RestController("sysAuditorLogAdminController")
@RequestMapping("/wk/sys/audit")
@RequiredArgsConstructor
public class AuditorLogAdminController {

    private final AuditLogRepository auditLogRepository;

    /**
     * 我的活动日志
     */
    @GetMapping("/my-activities")
    @Dictionary
    public Response<Paged<AuditLogVo>> myAuditLogs(AuditLogQuery query, CurrentUser user, QuickSearch qs) {
        query.setUserId(user.getUid());
        query.setTenantId(null);

        val logs = auditLogRepository.myAuditLogs(query, qs);

        return Response.success(
                PageUtil.from(logs, logs.getRecords(), (records) -> BeanUtil.copyToList(records, AuditLogVo.class)));
    }

    /**
     * 审计日志
     */
    @GetMapping("/activities")
    @Dictionary
    @PreAuthorize("@authz.iCan('r:sys.audit')")
    public Response<Paged<AuditLogVo>> activities(AuditLogQuery query, QuickSearch qs) {
        val tenantId = TenantManager.tenantId("0");
        if (!"0".equals(tenantId)) {
            if (StringUtils.isBlank(query.getTenantId()) || !tenantId.equals(query.getTenantId())) {
                query.setTenantId(tenantId);
            }
        }

        val logs = auditLogRepository.search(query, qs);

        return Response.success(
                PageUtil.from(logs, logs.getRecords(), (records) -> BeanUtil.copyToList(records, AuditLogVo.class)));
    }

}

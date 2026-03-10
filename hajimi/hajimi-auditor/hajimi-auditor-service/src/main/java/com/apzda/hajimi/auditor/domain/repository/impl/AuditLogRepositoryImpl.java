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
package com.apzda.hajimi.auditor.domain.repository.impl;

import com.apzda.hajimi.auditor.data.dto.AuditLogQuery;
import com.apzda.hajimi.auditor.domain.entity.AuditEntity;
import com.apzda.hajimi.auditor.domain.mapper.AuditEntityMapper;
import com.apzda.hajimi.auditor.domain.repository.AuditLogRepository;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
public class AuditLogRepositoryImpl extends ServiceImpl<AuditEntityMapper, AuditEntity> implements AuditLogRepository {

    @Override
    public IPage<AuditEntity> myAuditLogs(@Nonnull AuditLogQuery query, QuickSearch qs) {
        if (StringUtils.isBlank(query.getUserId())) {
            return new Page<>(query.getPageNumber(), query.getPageSize());
        }

        return search(query, qs);
    }

    @Override
    public IPage<AuditEntity> search(AuditLogQuery query, QuickSearch qs) {
        IPage<AuditEntity> page = PageUtil.from(query);

        return this.lambdaQuery()
            .eq(StringUtils.isNotBlank(query.getUserId()), AuditEntity::getUserId, query.getUserId())
            .eq(StringUtils.isNotBlank(query.getActivity()), AuditEntity::getActivity, query.getActivity())
            .eq(StringUtils.isNotBlank(query.getRunas()), AuditEntity::getRunas, query.getRunas())
            .eq(StringUtils.isNotBlank(query.getDevice()), AuditEntity::getDevice, query.getDevice())
            .eq(StringUtils.isNotBlank(query.getTenantId()), AuditEntity::getTenantId, query.getTenantId())
            .ge(query.getStartTime() != null, AuditEntity::getLogTime, query.getStartTime())
            .le(query.getEndTime() != null, AuditEntity::getLogTime, query.getEndTime())
            .and(qs.isEnabled(), (wrapper) -> {
                for (String field : qs.getFields()) {
                    switch (field) {
                        case "activity":
                            wrapper.or((q) -> q.eq(AuditEntity::getActivity, qs.getKeyword()));
                            break;
                        case "target":
                            wrapper.or((q) -> q.eq(AuditEntity::getTarget, qs.getKeyword()));
                            break;
                        case "level":
                            wrapper.or((q) -> q.eq(AuditEntity::getLevel, qs.getKeyword()));
                    }
                }
            })
            .page(page);
    }

}

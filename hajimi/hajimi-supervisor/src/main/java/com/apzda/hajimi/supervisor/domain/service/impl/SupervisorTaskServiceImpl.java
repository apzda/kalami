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
package com.apzda.hajimi.supervisor.domain.service.impl;

import com.apzda.hajimi.supervisor.Task;
import com.apzda.hajimi.supervisor.domain.entity.SupervisorTask;
import com.apzda.hajimi.supervisor.domain.mapper.SupervisorTaskMapper;
import com.apzda.hajimi.supervisor.domain.service.ISupervisorTaskService;
import com.apzda.hajimi.supervisor.domain.vo.TaskStatus;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUserProvider;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SupervisorTaskServiceImpl extends ServiceImpl<SupervisorTaskMapper, SupervisorTask>
        implements ISupervisorTaskService {

    private final ObjectMapper objectMapper;

    @Override
    public SupervisorTask getByStatusAndRunAtLe(TaskStatus status, long runAt, int sharding) {
        val con = Wrappers.lambdaQuery(SupervisorTask.class);
        con.eq(SupervisorTask::getStatus, status);
        con.eq(SupervisorTask::getSharding, sharding);
        con.le(SupervisorTask::getRunAt, runAt);
        con.orderByAsc(SupervisorTask::getRunAt);
        con.last("LIMIT 1");

        return getOne(con);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateStatus(@Nonnull SupervisorTask task, TaskStatus status) {
        val con = Wrappers.lambdaUpdate(SupervisorTask.class);
        con.eq(SupervisorTask::getStatus, status);
        con.eq(SupervisorTask::getSharding, task.getSharding());
        con.eq(SupervisorTask::getId, task.getId());

        return update(task, con);
    }

    @Override
    public <T> SupervisorTask submit(@Nonnull String supervisor, @Nonnull Task<T> task, @Nonnull TaskStatus status) {
        val supervisorTask = new SupervisorTask();
        supervisorTask.setRunAt(task.getRunAt().toEpochSecond(ZoneOffset.UTC));
        supervisorTask.setName(task.getName());
        supervisorTask.setSupervisor(supervisor);
        supervisorTask.setStatus(status);
        supervisorTask.setTenantId(TenantManager.tenantId());
        supervisorTask.setUid(CurrentUserProvider.getCurrentUser().getUid());
        supervisorTask.setSharding(task.sharding());
        try {
            if (isStringWorkload(task)) {
                supervisorTask.setContent((String) task.getWorkload());
            }
            else {
                supervisorTask.setContent(objectMapper.writeValueAsString(task.getWorkload()));
            }
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        supervisorTask.setRetries(0);
        if (!save(supervisorTask)) {
            throw new IllegalStateException("无法保存任务");
        }

        return supervisorTask;
    }

    private boolean isStringWorkload(@Nonnull Task<?> task) {
        val resolvableType = ResolvableType.forClass(Task.class, task.getClass());
        val resolved = resolvableType.getGeneric(0).resolve();
        return resolved != null && resolved.isAssignableFrom(String.class);
    }

}

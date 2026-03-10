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
package com.apzda.hajimi.supervisor;

import com.apzda.hajimi.supervisor.domain.service.ISupervisorTaskService;
import com.apzda.kalami.exception.StopRetryException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.val;
import org.springframework.core.ResolvableType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Getter
public abstract class AbstractSupervisor<T> implements ISupervisor<T> {

    private final String name;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private ISupervisorTaskService supervisorTaskService;

    protected final boolean stringWorkload;

    public AbstractSupervisor(String name) {
        this.name = name;
        val resolvableType = ResolvableType.forClass(ISupervisor.class, getClass());
        val resolved = resolvableType.getGeneric(0).resolve();
        stringWorkload = resolved != null && resolved.isAssignableFrom(String.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T parse(String workload) {
        try {
            if (stringWorkload) {
                return (T) workload;
            }
            return objectMapper.readValue(workload, new TypeReference<>() {
            });
        }
        catch (JsonProcessingException e) {
            throw new StopRetryException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.SUPPORTS)
    public long submit(@Nonnull Task<T> task) {
        return supervisorTaskService.submit(getName(), task).getId();
    }

}

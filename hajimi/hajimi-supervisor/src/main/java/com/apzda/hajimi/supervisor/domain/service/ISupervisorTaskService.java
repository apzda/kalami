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
package com.apzda.hajimi.supervisor.domain.service;

import com.apzda.hajimi.supervisor.Task;
import com.apzda.hajimi.supervisor.domain.entity.SupervisorTask;
import com.apzda.hajimi.supervisor.domain.vo.TaskStatus;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.annotation.Nonnull;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface ISupervisorTaskService extends IService<SupervisorTask> {

    SupervisorTask getByStatusAndRunAtLe(TaskStatus taskStatus, long runAt, int sharding);

    boolean updateStatus(SupervisorTask task, TaskStatus taskStatus);

    default <T> SupervisorTask submit(@Nonnull String supervisor, @Nonnull Task<T> task) {
        return submit(supervisor, task, TaskStatus.PENDING);
    }

    <T> SupervisorTask submit(@Nonnull String supervisor, @Nonnull Task<T> task, @Nonnull TaskStatus status);

}

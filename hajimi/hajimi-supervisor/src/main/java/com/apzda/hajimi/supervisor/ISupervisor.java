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

import jakarta.annotation.Nonnull;

import java.time.LocalDateTime;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface ISupervisor<T> {

    String getName();

    default boolean check(String workload) {
        return doCheck(parse(workload));
    }

    boolean doCheck(T workload);

    T parse(String workload);

    long submit(@Nonnull Task<T> task);

    default long runAt(String name, @Nonnull LocalDateTime runAt, @Nonnull T workload) {
        return submit(new Task<>(name, runAt, workload));
    }

    default long runAt(String name, @Nonnull T workload) {
        return submit(new Task<>(name, LocalDateTime.now(), workload));
    }

}

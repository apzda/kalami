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
import lombok.Getter;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Getter
public class Task<T> {

    private final String name;

    private final LocalDateTime runAt;

    private final T workload;

    public Task(@Nonnull String name, @Nonnull LocalDateTime runAt, @Nonnull T workload) {
        Assert.hasText(name, "name must not be empty");
        Assert.notNull(runAt, "runAt must not be null");
        Assert.notNull(workload, "workload must not be null");
        this.name = name;
        this.runAt = runAt;
        this.workload = workload;
    }

    public Task(String name, Duration runAt, @Nonnull T workload) {
        this(name, LocalDateTime.now().plus(runAt), workload);
    }

    public Task(String name, @Nonnull T workload) {
        this(name, LocalDateTime.now(), workload);
    }

    public int sharding() {
        return 0;
    }

}

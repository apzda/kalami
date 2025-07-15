/*
 * Copyright 2025 the original author or authors.
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
package com.apzda.kalami.state;

import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 状态守卫
 *
 * @author john <service@cheerel.com>
 */
@Data
@NoArgsConstructor
public class StateGuard<S extends Enum<S>> {

    /**
     * 前置状态列表
     */
    private List<S> preStateList;

    /**
     * 下一个状态
     */
    private S nextState;

    public StateGuard(@Nullable List<S> preStateList, @Nullable S nextState) {
        this.preStateList = preStateList;
        this.nextState = nextState;
    }

}

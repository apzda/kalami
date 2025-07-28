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

/**
 * 状态守卫
 *
 * @author john <service@cheerel.com>
 */
@Data
public class StateGuard<S extends Enum<S>> {

    /**
     * 前置状态
     */
    private S preState;

    /**
     * 下一个状态
     */
    private S nextState;

    public StateGuard(@Nullable S preState, @Nullable S nextState) {
        this.preState = preState;
        this.nextState = nextState;
    }

    public static <S extends Enum<S>> StateGuard<S> of(S preState, S nextState) {
        return new StateGuard<>(preState, nextState);
    }

}

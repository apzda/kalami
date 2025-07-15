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

import com.apzda.kalami.utils.ComTool;
import lombok.Getter;

/**
 * @author john <service@cheerel.com>
 */
@Getter
public abstract class AbstractStateMachine<State extends Enum<State>, Event extends Enum<Event>>
        implements StateMachine<State, Event> {

    @Override
    public void handleEvent(Event event) {
        StateGuard<State> guard = getEventGuards().get(event);
        if (guard != null) {
            if (guard.getPreStateList() == null || guard.getPreStateList().isEmpty()
                    || guard.getPreStateList().contains(getCurrentState())) {
                if (guard.getNextState() != null) {
                    stateTransition(guard.getNextState());
                }
                return;
            }
        }
        throw new RuntimeException(ComTool.format("处理失败-[错误的状态变更], currentState={}, event={}", getCurrentState(), event));
    }

}

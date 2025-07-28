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

import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.utils.ComTool;

/**
 * @author john <service@cheerel.com>
 */
public abstract class AbstractStateMachine<T extends StateEntity<State>, State extends Enum<State>, Event extends Enum<Event>>
        implements StateMachine<T, State, Event> {

    /**
     * 状态实体
     */
    private final T stateEntity;

    /**
     * 老的状态（变更前的状态）
     */
    private State oldState;

    public AbstractStateMachine(T stateEntity) {
        this.stateEntity = stateEntity;
    }

    @Override
    public T getStateEntity() {
        return stateEntity;
    }

    @Override
    public final void trigger(Event event) {
        StateGuard<State> stateGuard = getEventGuards().get(event);
        if (stateGuard != null) {
            if (stateGuard.getPreState() == null || stateGuard.getPreState() == stateEntity.getState()) {
                // 设置变动前的状态
                this.oldState = stateEntity.getState();
                if (stateGuard.getNextState() != null) {
                    // 状态变更
                    stateEntity.setState(stateGuard.getNextState());
                }
                return;
            }
        }
        throw new CommonBizException(ComTool.format("处理失败-[错误的状态变更], stateEntity={}, event={}", stateEntity, event));
    }

    @Override
    public State getOldState() {
        return oldState;
    }

    @Override
    public State getCurrentState() {
        return stateEntity.getState();
    }

}

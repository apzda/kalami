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

import java.util.Map;

/**
 * 状态机
 *
 * @author john <service@cheerel.com>
 */
public interface StateMachine<State extends Enum<State>, Event extends Enum<Event>> {

    /**
     * 事件处理
     * @param event 事件
     */
    void handleEvent(Event event);

    /**
     * 获取当前状态
     */
    State getCurrentState();

    /**
     * 状态转换
     * @param nextState 转换后的状态
     */
    void stateTransition(State nextState);

    /**
     * 获取事件守卫
     */
    Map<Event, StateGuard<State>> getEventGuards();

}

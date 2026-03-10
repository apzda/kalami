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
package com.apzda.hajimi.ucenter.listener;

import com.apzda.hajimi.ucenter.domain.event.PrivilegeDeletedEvent;
import com.apzda.hajimi.ucenter.domain.event.PrivilegeEvent;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.RolePrivilegeMapper;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PrivilegeEventListener implements ApplicationListener<PrivilegeEvent> {

    private final RolePrivilegeMapper rolePrivilegeMapper;

    @Override
    public void onApplicationEvent(@Nonnull PrivilegeEvent event) {
        val privilege = event.getPrivilege();
        if (event instanceof PrivilegeDeletedEvent) {
            rolePrivilegeMapper.revokeByPrivilegeId(privilege.getId());
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return false;
    }

}

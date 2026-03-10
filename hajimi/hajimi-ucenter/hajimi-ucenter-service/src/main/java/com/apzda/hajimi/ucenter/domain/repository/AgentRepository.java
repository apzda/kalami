/*
 * Copyright 2025-2026 the original author or authors.
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

package com.apzda.hajimi.ucenter.domain.repository;

import com.apzda.hajimi.ucenter.controller.admin.dto.AgentPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.AgentEntity;
import com.apzda.hajimi.ucenter.service.dto.AgentDto;
import com.apzda.kalami.agent.Agent;
import com.apzda.kalami.data.QuickSearch;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.repository.IRepository;
import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface AgentRepository extends IRepository<AgentEntity> {

    IPage<AgentEntity> pageQuery(AgentPageQuery query, QuickSearch quickSearch);

    @Nonnull
    AgentEntity create(AgentDto dto);

    @Nonnull
    AgentEntity update(AgentDto dto);

    List<Agent> listChildren(String parentId);

    boolean phoneExist(String phone);

    AgentEntity getByUid(String uid);

}

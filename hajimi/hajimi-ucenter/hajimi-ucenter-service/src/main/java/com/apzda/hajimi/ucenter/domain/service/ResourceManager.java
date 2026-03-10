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
package com.apzda.hajimi.ucenter.domain.service;

import com.apzda.hajimi.ucenter.domain.entity.RbacResourceEntity;
import com.apzda.hajimi.ucenter.domain.repository.ResourceRepository;
import com.apzda.hajimi.ucenter.domain.vo.ResourceVo;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ResourceManager {

    private final ResourceRepository resourceRepository;

    private final UCenterConverter ucenterConverter;

    public List<ResourceVo> listByPid(String pid) {
        List<ResourceVo> resources = new ArrayList<>();
        List<RbacResourceEntity> entities = resourceRepository.listByPid(pid);
        if (CollectionUtils.isEmpty(entities)) {
            return resources;
        }
        for (RbacResourceEntity entity : entities) {
            val vo = ucenterConverter.from(entity);
            if (Boolean.TRUE.equals(vo.getHasChildren())) {
                vo.setChildren(listByPid(vo.getId()));
            }
            resources.add(vo);
        }

        return resources;
    }

    public List<ResourceVo> mySubscribed(List<String> subscriptions) {
        return listByPid("0", subscriptions);
    }

    @Nonnull
    private List<ResourceVo> listByPid(String pid, List<String> subscriptions) {
        List<ResourceVo> resources = new ArrayList<>();
        List<RbacResourceEntity> entities = resourceRepository.listByPidAndServices(pid, subscriptions);
        if (CollectionUtils.isEmpty(entities)) {
            return resources;
        }
        for (RbacResourceEntity entity : entities) {
            val vo = ucenterConverter.from(entity);
            if (Boolean.TRUE.equals(vo.getHasChildren())) {
                vo.setChildren(listByPid(vo.getId(), subscriptions));
            }
            resources.add(vo);
        }

        return resources;
    }

}

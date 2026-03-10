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
package com.apzda.hajimi.ucenter.domain.repository;

import com.apzda.hajimi.ucenter.controller.admin.dto.ResourceDto;
import com.apzda.hajimi.ucenter.domain.entity.RbacResourceEntity;
import com.baomidou.mybatisplus.extension.repository.IRepository;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface ResourceRepository extends IRepository<RbacResourceEntity> {

    default List<RbacResourceEntity> listByPid(String pid) {
        return listByPid(pid, null);
    }

    List<RbacResourceEntity> listByPid(String pid, Map<String, String> param);

    List<RbacResourceEntity> listByPidAndServices(String pid, List<String> subscriptions);

    boolean create(ResourceDto dto);

    boolean update(ResourceDto dto);

    void deleteById(String id);

    RbacResourceEntity findByPidAndRid(Serializable id, String rid);

    RbacResourceEntity findByPermission(@NotBlank String permission);

}

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

import com.apzda.hajimi.ucenter.controller.admin.dto.RoleDto;
import com.apzda.hajimi.ucenter.controller.admin.dto.RolePageQuery;
import com.apzda.hajimi.ucenter.domain.entity.RbacPrivilegeEntity;
import com.apzda.hajimi.ucenter.domain.entity.RoleEntity;
import com.apzda.hajimi.ucenter.domain.entity.RolePrivilegeEntity;
import com.apzda.kalami.data.QuickSearch;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.repository.IRepository;
import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface RoleRepository extends IRepository<RoleEntity> {

    @Nonnull
    IPage<RoleEntity> pageQuery(RolePageQuery query);

    @Nonnull
    IPage<RoleEntity> pageQuery(RolePageQuery query, QuickSearch quickSearch);

    @Nonnull
    List<RoleEntity> parents(Long roleId);

    boolean create(RoleDto dto);

    boolean update(RoleDto dto);

    boolean delete(Long id);

    RoleEntity findByRoleAndTenantId(String role, String tenantId);

    List<RoleEntity> listLeasableRoles();

    List<RbacPrivilegeEntity> listPrivilegesById(Long roleId, String tenantId);

    void grants(List<RolePrivilegeEntity> privileges);

    RoleEntity getByTenantAndId(String tenantId, String id);

}

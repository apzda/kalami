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
package com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper;

import com.apzda.hajimi.ucenter.domain.entity.RbacPrivilegeEntity;
import com.apzda.hajimi.ucenter.domain.entity.RolePrivilegeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface RolePrivilegeMapper extends BaseMapper<RolePrivilegeEntity> {

    List<RbacPrivilegeEntity> listPrivilegesByRoleIds(@Param("roleIds") List<Serializable> roleIds,
            @Param("tenantId") Serializable tenantId);

    int revokeByPrivilegeId(@Param("privilegeId") Long privilegeId);

    boolean existsByRoleIdAndPrivilegeId(@Param("roleId") Long roleId, @Param("privilegeId") Long privilegeId);

    int deleteByRoleId(Long id);

    int deleteByRoleIdAndPrivilegeId(@Param("roleId") Long roleId, @Param("privilegeId") Long privilegeId);

}

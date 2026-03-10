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

import com.apzda.hajimi.ucenter.domain.entity.OrganizationEntity;
import com.apzda.hajimi.ucenter.domain.entity.OrganizationUserEntity;
import com.apzda.hajimi.ucenter.domain.vo.OrganizationVo;
import com.apzda.hajimi.ucenter.service.dto.OrganizationDto;
import com.apzda.kalami.tenant.Organization;
import com.baomidou.mybatisplus.extension.repository.IRepository;
import jakarta.annotation.Nonnull;

import java.io.Serializable;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface OrganizationRepository extends IRepository<OrganizationEntity> {

    OrganizationEntity getByTenantIdAndId(Serializable tenantId, Serializable orgId);

    OrganizationEntity getByCode(String tenantId, String code);

    List<OrganizationVo> listOrganizationByTidAndUid(String tenantId, String uid);

    OrganizationUserEntity findUserByTidAndOrgIdAndUid(Long tenantId, Long orgId, Long uid);

    OrganizationUserEntity findUserByTidAndOrgIdAndUidIgnoreDeleted(Long tenantId, Long orgId, Long uid);

    boolean switchTo(OrganizationEntity organization, Long uid);

    List<Organization> listChildren(String tenantId, Long parentId);

    @Nonnull
    OrganizationEntity create(OrganizationDto dto);

    @Nonnull
    OrganizationEntity update(OrganizationDto dto);

    OrganizationUserEntity join(Long tenantId, Long orgId, Long uid);

    boolean leave(Long tenantId, Long orgId, Long uid);

    boolean recover(Long tenantId, Long orgId, Long uid);

}

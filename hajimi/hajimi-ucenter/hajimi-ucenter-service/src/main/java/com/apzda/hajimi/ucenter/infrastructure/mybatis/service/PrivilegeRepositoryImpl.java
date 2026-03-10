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
package com.apzda.hajimi.ucenter.infrastructure.mybatis.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.apzda.hajimi.ucenter.config.UCenterTenantConfigureProperties;
import com.apzda.hajimi.ucenter.controller.admin.dto.PrivilegeDto;
import com.apzda.hajimi.ucenter.controller.admin.dto.PrivilegePageQuery;
import com.apzda.hajimi.ucenter.domain.entity.RbacPrivilegeEntity;
import com.apzda.hajimi.ucenter.domain.entity.RbacResourceEntity;
import com.apzda.hajimi.ucenter.domain.event.PrivilegeDeletedEvent;
import com.apzda.hajimi.ucenter.domain.event.PrivilegeModifiedEvent;
import com.apzda.hajimi.ucenter.domain.repository.PrivilegeRepository;
import com.apzda.hajimi.ucenter.domain.repository.ResourceRepository;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.RbacPrivilegeMapper;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.error.DuplicateError;
import com.apzda.kalami.error.ResourceUpdateError;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class PrivilegeRepositoryImpl extends ServiceImpl<RbacPrivilegeMapper, RbacPrivilegeEntity>
        implements PrivilegeRepository {

    private final UCenterTenantConfigureProperties properties;

    private final ResourceRepository resourceRepository;

    private final ApplicationEventPublisher publisher;

    @Override
    public IPage<RbacPrivilegeEntity> pageQuery(PrivilegePageQuery query, QuickSearch quickSearch) {
        IPage<RbacPrivilegeEntity> pager = PageUtil.from(query);
        val order = new OrderItem();
        order.setColumn("builtin");
        order.setAsc(true);
        pager.orders().add(0, order);
        if (quickSearch != null && quickSearch.isEnabled()) {
            val q = quickSearch.getKeyword();
            val fields = quickSearch.getFields();
            if (fields.contains("name")) {
                query.setName(q);
            }
            if (fields.contains("perm")) {
                query.setPermission(q);
            }
        }
        return this.lambdaQuery()
            .eq(RbacPrivilegeEntity::getTenantId, query.getTenantId())
            .eq(StringUtils.isNotBlank(query.getId()), RbacPrivilegeEntity::getId, query.getId())
            .eq(StringUtils.isNotBlank(query.getType()), RbacPrivilegeEntity::getType, query.getType())
            .and(!StringUtils.isAllBlank(query.getName(), query.getPermission()), (where) -> {
                if (StringUtils.isNotBlank(query.getName())) {
                    where.or((con) -> con.like(RbacPrivilegeEntity::getName, query.getName()));
                }
                if (StringUtils.isNotBlank(query.getPermission())) {
                    where.or((con) -> con.like(RbacPrivilegeEntity::getPermission, query.getPermission()));
                }
            })
            .page(pager);
    }

    @Override
    @Transactional
    public boolean create(PrivilegeDto dto) {
        RbacPrivilegeEntity privilege = new RbacPrivilegeEntity();
        BeanUtil.copyProperties(dto, privilege);
        val entity = findByPermissionAndTenantId(dto.getPermission(), dto.getTenantId());
        if (entity != null) {
            throw new CommonBizException(
                    new DuplicateError(String.format("【%s】已被权限【%s】定义", dto.getPermission(), entity.getName())));
        }
        RbacResourceEntity resource = resourceRepository.findByPermission(dto.getPermission());
        if (resource != null) {
            privilege.setService(resource.getService());
        }
        privilege.setId(null);
        return this.save(privilege);
    }

    @Override
    @Transactional
    public boolean update(@Nonnull PrivilegeDto dto) {
        val entity = findByPermissionAndTenantId(dto.getPermission(), dto.getTenantId());
        if (entity != null && !entity.getId().equals(dto.getId())) {
            throw new CommonBizException(
                    new DuplicateError(String.format("【%s】已被权限【%s】定义", dto.getPermission(), entity.getName())));
        }

        RbacPrivilegeEntity privilege = new RbacPrivilegeEntity();
        BeanUtil.copyProperties(dto, privilege, CopyOptions.create().setIgnoreNullValue(true));
        RbacResourceEntity resource = resourceRepository.findByPermission(dto.getPermission());
        if (resource != null) {
            privilege.setService(resource.getService());
        }
        if (this.updateById(privilege)) {
            publisher.publishEvent(new PrivilegeModifiedEvent(privilege));
            return true;
        }
        throw new CommonBizException(new ResourceUpdateError("权限", dto.getName()));
    }

    @Override
    @Transactional
    public boolean delete(@Nonnull RbacPrivilegeEntity privilege) {
        if (this.baseMapper.deletePrivilegeById(String.valueOf(privilege.getId())) > 0) {
            publisher.publishEvent(new PrivilegeDeletedEvent(privilege));
            return true;
        }
        return false;
    }

    @Override
    public RbacPrivilegeEntity findByPermissionAndTenantId(String permission, String tenantId) {
        return lambdaQuery().eq(RbacPrivilegeEntity::getPermission, permission)
            .eq(RbacPrivilegeEntity::getTenantId, tenantId)
            .one();
    }

    @Override
    public void sync(Long tenantId) {

        CompletableFuture.runAsync(() -> {
            val where = Wrappers.lambdaQuery(RbacPrivilegeEntity.class)
                .eq(RbacPrivilegeEntity::getTenantId, "0")
                .isNotNull(RbacPrivilegeEntity::getService);
            val privileges = list(where);
            if (!privileges.isEmpty()) {
                val service = properties.getService();

            }
        });
    }

}

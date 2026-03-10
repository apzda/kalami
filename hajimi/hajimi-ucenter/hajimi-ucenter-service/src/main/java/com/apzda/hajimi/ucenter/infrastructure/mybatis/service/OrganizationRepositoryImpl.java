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
import cn.hutool.core.util.StrUtil;
import com.apzda.hajimi.ucenter.controller.admin.dto.OrganizationUserPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.OrganizationEntity;
import com.apzda.hajimi.ucenter.domain.entity.OrganizationUserEntity;
import com.apzda.hajimi.ucenter.domain.repository.OrganizationRepository;
import com.apzda.hajimi.ucenter.domain.repository.TenantRepository;
import com.apzda.hajimi.ucenter.domain.vo.OrganizationVo;
import com.apzda.hajimi.ucenter.event.OrganizationCreatedEvent;
import com.apzda.hajimi.ucenter.event.OrganizationModifiedEvent;
import com.apzda.hajimi.ucenter.event.OrganizationSwitchedEvent;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.OrganizationMapper;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.OrganizationUserMapper;
import com.apzda.hajimi.ucenter.service.dto.OrganizationDto;
import com.apzda.kalami.error.DuplicateError;
import com.apzda.kalami.error.ResourceNotFoundError;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.tenant.Organization;
import com.apzda.kalami.utils.TreeUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class OrganizationRepositoryImpl extends ServiceImpl<OrganizationMapper, OrganizationEntity>
        implements OrganizationRepository {

    private final OrganizationMapper organizationMapper;

    private final OrganizationUserMapper organizationUserMapper;

    private final ApplicationContext applicationContext;

    private final ApplicationEventPublisher publisher;

    @Override
    public OrganizationEntity getByTenantIdAndId(Serializable tenantId, Serializable orgId) {
        return lambdaQuery().eq(OrganizationEntity::getTenantId, tenantId).eq(OrganizationEntity::getId, orgId).one();
    }

    @Override
    public OrganizationEntity getByCode(String tenantId, String code) {
        return lambdaQuery().eq(OrganizationEntity::getTenantId, tenantId).eq(OrganizationEntity::getCode, code).one();
    }

    @Override
    @Cacheable(cacheNames = "uc:org:users", key = "#tenantId+'.'+#uid")
    public List<OrganizationVo> listOrganizationByTidAndUid(String tenantId, String uid) {
        val query = new OrganizationUserPageQuery();
        query.setTenantId(tenantId);
        query.setUid(uid);
        query.setPageNumber(1);
        query.setPageSize(500);
        query.setPageSorts("current|DESC,id|ASC");
        IPage<OrganizationVo> pager = PageUtil.from(query, false);
        return organizationMapper.users(pager, query).getRecords();
    }

    @Override
    public OrganizationUserEntity findUserByTidAndOrgIdAndUid(Long tenantId, Long orgId, Long uid) {
        val where = Wrappers.lambdaQuery(OrganizationUserEntity.class)
            .eq(OrganizationUserEntity::getTenantId, tenantId)
            .eq(OrganizationUserEntity::getOrgId, orgId)
            .eq(OrganizationUserEntity::getUid, uid);
        return this.organizationUserMapper.selectOne(where);
    }

    @Override
    public OrganizationUserEntity findUserByTidAndOrgIdAndUidIgnoreDeleted(Long tenantId, Long orgId, Long uid) {
        return this.organizationUserMapper.findUserByTidAndOrgIdAndUidIgnoreDeleted(tenantId, orgId, uid);
    }

    @Override
    @Nonnull
    public OrganizationEntity create(@Nonnull OrganizationDto dto) {
        val code = dto.getCode();
        val tenantId = dto.getTenantId();
        val old = getByCode(tenantId, dto.getCode());
        if (old != null) {
            throw new CommonBizException(new DuplicateError(StrUtil.format("'{}'代码已存在", code)));
        }
        val tenantRepository = applicationContext.getBean(TenantRepository.class);
        val tenant = tenantRepository.getById(tenantId);
        if (tenant == null) {
            throw new CommonBizException(new ResourceNotFoundError("租户", tenantId));
        }

        val parentId = Optional.ofNullable(dto.getParentId()).orElse(0L);
        if (parentId != 0) {
            val parent = getById(parentId);
            if (parent == null) {
                throw new CommonBizException(String.format("上级部门%d不存在", parentId));
            }
            if (!parent.getTenantId().equals(tenantId)) {
                throw new CommonBizException("租户不一致");
            }
        }

        OrganizationEntity entity = new OrganizationEntity();
        entity.setTenantId(tenantId);
        entity.setParentId(parentId);
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setShortName(dto.getShortName());
        entity.setLogo(dto.getLogo());
        entity.setRemark(dto.getRemark());
        if (!save(entity)) {
            throw new CommonBizException(String.format("创建部门'%s'失败", dto.getName()));
        }

        Organization organization = new Organization();
        organization.setId(String.valueOf(entity.getId()));
        organization.setPid(String.valueOf(dto.getParentId()));
        organization.setTenantId(entity.getTenantId());
        organization.setName(dto.getName());
        organization.setShortName(dto.getShortName());
        organization.setDescription(dto.getRemark());
        organization.setCode(dto.getCode());
        organization.setLogo(dto.getLogo());
        // 发布事件
        publisher.publishEvent(new OrganizationCreatedEvent(organization));

        return entity;
    }

    @Override
    @Nonnull
    public OrganizationEntity update(@Nonnull OrganizationDto dto) {
        val tenantId = dto.getTenantId();
        val entity = getByTenantIdAndId(tenantId, dto.getId());
        if (entity == null) {
            throw new CommonBizException(new ResourceNotFoundError("部门", String.valueOf(dto.getId())));
        }

        val tenantRepository = applicationContext.getBean(TenantRepository.class);
        val tenant = tenantRepository.getById(tenantId);
        if (tenant == null) {
            throw new CommonBizException(new ResourceNotFoundError("租户", tenantId));
        }
        val parentId = dto.getParentId();
        if (parentId != null) {
            if (parentId.equals(entity.getId())) {
                throw new CommonBizException("平级从属关系异常");
            }
            else {
                val tenants = listChildren(entity.getTenantId(), entity.getId());
                if (TreeUtils.isMyChild(String.valueOf(parentId), tenants)) {
                    throw new CommonBizException("上下级从属关系异常");
                }
            }
        }
        BeanUtil.copyProperties(dto, entity,
                CopyOptions.create().setIgnoreNullValue(true).setIgnoreProperties("code", "pid"));

        if (!updateById(entity)) {
            throw new CommonBizException(String.format("修改部门'%s'失败", dto.getName()));
        }

        // 清缓存
        CompletableFuture.runAsync(() -> {
            try {
                val cm = this.applicationContext.getBean(CacheManager.class);
                val cache = cm.getCache("uc:org:users");
                if (cache != null) {
                    List<OrganizationUserEntity> users = organizationUserMapper
                        .listUsersByTenantIdAndOrgId(Long.valueOf(entity.getTenantId()), entity.getId());
                    users.forEach(user -> {
                        cache.evict(user.getTenantId() + "." + user.getUid());
                    });
                }
            }
            catch (Exception ignored) {
            }
        });

        Organization organization = new Organization();
        organization.setId(String.valueOf(entity.getId()));
        organization.setPid(String.valueOf(entity.getParentId()));
        organization.setTenantId(entity.getTenantId());
        organization.setName(entity.getName());
        organization.setShortName(entity.getShortName());
        organization.setDescription(entity.getRemark());
        organization.setCode(entity.getCode());
        organization.setLogo(entity.getLogo());
        // 发布事件
        publisher.publishEvent(new OrganizationModifiedEvent(organization));
        return entity;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "uc:org:users", key = "#tenantId+'.'+#uid")
    public OrganizationUserEntity join(Long tenantId, Long orgId, Long uid) {
        OrganizationUserEntity user = this.findUserByTidAndOrgIdAndUidIgnoreDeleted(tenantId, orgId, uid);
        if (user == null) {
            user = new OrganizationUserEntity();
            user.setTenantId(String.valueOf(tenantId));
            user.setOrgId(String.valueOf(orgId));
            user.setUid(uid);
            if (this.organizationUserMapper.insert(user) > 0) {
                return user;
            }
            throw new CommonBizException("用户加入部门失败");
        }
        else {
            if (recover(tenantId, orgId, uid)) {
                user.setDeleted(false);
                return user;
            }
            throw new CommonBizException("用户加入部门失败");
        }
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "uc:org:users", key = "#tenantId+'.'+#uid")
    public boolean leave(Long tenantId, Long orgId, Long uid) {
        this.organizationUserMapper.delete(Wrappers.lambdaQuery(OrganizationUserEntity.class)
            .eq(OrganizationUserEntity::getUid, uid)
            .eq(OrganizationUserEntity::getTenantId, tenantId)
            .eq(OrganizationUserEntity::getOrgId, orgId));
        return true;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "uc:org:users", key = "#tenantId+'.'+#uid")
    public boolean recover(Long tenantId, Long orgId, Long uid) {
        return this.organizationUserMapper.recover(tenantId, orgId, uid) > 0;
    }

    @Override
    @CacheEvict(cacheNames = "uc:org:users", key = "#organization.tenantId+'.'+#uid")
    public boolean switchTo(@Nonnull OrganizationEntity organization, Long uid) {
        val update1 = Wrappers.lambdaUpdate(OrganizationUserEntity.class)
            .eq(OrganizationUserEntity::getUid, uid)
            .eq(OrganizationUserEntity::getTenantId, organization.getTenantId())
            .ne(OrganizationUserEntity::getOrgId, organization.getId())
            .set(OrganizationUserEntity::getCurrent, false);
        this.organizationUserMapper.update(update1);

        val update2 = Wrappers.lambdaUpdate(OrganizationUserEntity.class)
            .eq(OrganizationUserEntity::getUid, uid)
            .eq(OrganizationUserEntity::getTenantId, organization.getTenantId())
            .eq(OrganizationUserEntity::getOrgId, organization.getId())
            .set(OrganizationUserEntity::getCurrent, true);

        this.organizationUserMapper.update(update2);

        // 发布部门切换成功事件
        val source = new Organization();
        source.setId(String.valueOf(organization.getId()));
        source.setPid(String.valueOf(organization.getParentId()));
        source.setTenantId(organization.getTenantId());
        source.setName(organization.getName());
        source.setShortName(organization.getShortName());
        source.setLogo(organization.getLogo());
        source.setCode(organization.getCode());
        source.setUid(String.valueOf(uid));
        publisher.publishEvent(new OrganizationSwitchedEvent(source));

        return true;
    }

    @Override
    public List<Organization> listChildren(String tenantId, Long parentId) {
        if (parentId == null) {
            return List.of();
        }
        return lambdaQuery().eq(OrganizationEntity::getTenantId, tenantId)
            .eq(OrganizationEntity::getParentId, parentId)
            .list()
            .stream()
            .map(organization -> {
                Organization org = new Organization();
                org.setId(String.valueOf(organization.getId()));
                org.setPid(String.valueOf(organization.getParentId()));
                org.setTenantId(organization.getTenantId());
                org.setName(organization.getName());
                org.setShortName(organization.getShortName());
                org.setDescription(organization.getRemark());
                org.setCode(organization.getCode());
                org.setLogo(organization.getLogo());
                org.setChildren(listChildren(tenantId, organization.getId()));
                return org;
            })
            .toList();
    }

}

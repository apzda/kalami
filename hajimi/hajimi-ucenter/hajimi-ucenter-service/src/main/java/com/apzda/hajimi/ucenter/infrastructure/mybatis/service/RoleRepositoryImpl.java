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
import com.apzda.hajimi.ucenter.controller.admin.dto.RoleDto;
import com.apzda.hajimi.ucenter.controller.admin.dto.RolePageQuery;
import com.apzda.hajimi.ucenter.domain.entity.RbacPrivilegeEntity;
import com.apzda.hajimi.ucenter.domain.entity.RoleChildrenEntity;
import com.apzda.hajimi.ucenter.domain.entity.RoleEntity;
import com.apzda.hajimi.ucenter.domain.entity.RolePrivilegeEntity;
import com.apzda.hajimi.ucenter.domain.repository.PrivilegeRepository;
import com.apzda.hajimi.ucenter.domain.repository.RoleRepository;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.RoleChildrenMapper;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.RoleMapper;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.RolePrivilegeMapper;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.error.IllegalChildError;
import com.apzda.kalami.error.ResourceCreateError;
import com.apzda.kalami.error.ResourceUpdateError;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoleRepositoryImpl extends ServiceImpl<RoleMapper, RoleEntity> implements RoleRepository {

    private final PrivilegeRepository privilegeRepository;

    private final RolePrivilegeMapper rolePrivilegeMapper;

    private final RoleChildrenMapper roleChildrenMapper;

    @Override
    @Nonnull
    public IPage<RoleEntity> pageQuery(RolePageQuery query) {
        IPage<RoleEntity> pager = PageUtil.from(query);
        pager.orders().add(0, new OrderItem().setColumn("builtin").setAsc(true));
        return this.lambdaQuery()
            .eq(RoleEntity::getTenantId, query.getTenantId())
            .eq(StringUtils.isNotBlank(query.getId()), RoleEntity::getId, query.getId())
            .and(!StringUtils.isAllBlank(query.getRole(), query.getName()), (where) -> {
                if (StringUtils.isNotBlank(query.getRole())) {
                    where.or((con) -> con.like(RoleEntity::getRole, query.getRole()));
                }
                if (StringUtils.isNotBlank(query.getName())) {
                    where.or((con) -> con.like(RoleEntity::getName, query.getName()));
                }
            })
            .page(pager);
    }

    @Override
    public @Nonnull IPage<RoleEntity> pageQuery(RolePageQuery query, QuickSearch quickSearch) {
        IPage<RoleEntity> pager = PageUtil.from(query);
        pager.orders().add(0, new OrderItem().setColumn("builtin").setAsc(true));

        if (quickSearch != null && quickSearch.isEnabled()) {
            val q = quickSearch.getKeyword();
            val fields = quickSearch.getFields();
            if (fields.contains("role")) {
                query.setRole(q);
            }
            if (fields.contains("name")) {
                query.setName(q);
            }
        }
        val id = query.getId();
        return this.lambdaQuery()
            .eq(RoleEntity::getTenantId, query.getTenantId())
            .eq(query.getLeasable() != null, RoleEntity::getLeasable, query.getLeasable())
            .and(StringUtils.isNotBlank(id), (where) -> {
                where.eq(Strings.CS.startsWith(id, "!"), RoleEntity::getId, id);
                where.ne(Strings.CS.startsWith(id, "!"), RoleEntity::getId, id.substring(1));
            })
            .and(!StringUtils.isAllBlank(query.getRole(), query.getName()), (where) -> {
                if (StringUtils.isNotBlank(query.getRole())) {
                    where.or((con) -> con.like(RoleEntity::getRole, query.getRole()));
                }
                if (StringUtils.isNotBlank(query.getName())) {
                    where.or((con) -> con.like(RoleEntity::getName, query.getName()));
                }
            })
            .page(pager);
    }

    @Override
    public List<RoleEntity> listLeasableRoles() {
        return this.lambdaQuery().eq(RoleEntity::getTenantId, "0").eq(RoleEntity::getLeasable, true).list();
    }

    @Override
    public List<RbacPrivilegeEntity> listPrivilegesById(Long roleId, String tenantId) {
        return this.rolePrivilegeMapper.listPrivilegesByRoleIds(List.of(roleId), tenantId);
    }

    @Override
    public void grants(List<RolePrivilegeEntity> privileges) {
        this.rolePrivilegeMapper.insert(privileges);
    }

    @Override
    public RoleEntity getByTenantAndId(String tenantId, String id) {
        return lambdaQuery().eq(RoleEntity::getTenantId, tenantId).eq(RoleEntity::getId, id).one();
    }

    @Override
    @Transactional
    public boolean create(@Nonnull RoleDto dto) {
        RoleEntity role = new RoleEntity();
        role.setTenantId(dto.getTenantId());
        role.setRole(dto.getRole());
        role.setName(dto.getName());
        role.setLanding(dto.getLanding());
        role.setBuiltin(false);
        if (Objects.equals("0", dto.getTenantId()) || StringUtils.isBlank(dto.getTenantId())) {
            role.setLeasable(dto.getLeasable());
        }
        else {
            role.setLeasable(true);
        }
        role.setProvider(UserToken.DEFAULT_PROVIDER);
        role.setDescription(dto.getDescription());
        if (!save(role)) {
            new ResourceCreateError("角色").emit();
        }
        if (!CollectionUtils.isEmpty(dto.getGranted())) {
            updateRolePrivileges(dto.getGranted(), role);
        }

        if (!CollectionUtils.isEmpty(dto.getChildren())) {
            updateRoleChildren(dto.getChildren(), role);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean update(RoleDto dto) {
        RoleEntity role = new RoleEntity();
        BeanUtil.copyProperties(dto, role, CopyOptions.create().setIgnoreNullValue(true));

        if (!updateById(role)) {
            new ResourceUpdateError("角色", dto.getId().toString()).emit();
        }

        updateRolePrivileges(dto.getGranted(), role);
        updateRoleChildren(dto.getChildren(), role);

        return true;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        // 删除本体
        this.baseMapper.deleteRoleById(id);
        // 删除授权
        this.rolePrivilegeMapper.deleteByRoleId(id);
        // 删除子角色
        this.roleChildrenMapper.deleteByRoleId(id);

        return true;
    }

    @Override
    @Nonnull
    public List<RoleEntity> parents(Long roleId) {
        Set<RoleEntity> parents = new HashSet<>();
        val p = roleChildrenMapper.parents(roleId);
        if (!CollectionUtils.isEmpty(p)) {
            for (RoleEntity role : p) {
                if (parents.contains(role)) {
                    continue;
                }
                parents.add(role);
                parents.addAll(parents(role.getId()));
            }
        }
        return parents.stream().toList();
    }

    @Override
    public RoleEntity findByRoleAndTenantId(String role, String tenantId) {
        return lambdaQuery().eq(RoleEntity::getRole, role).eq(RoleEntity::getTenantId, tenantId).one();
    }

    /**
     * 更新角色的授权
     */
    private void updateRolePrivileges(@Nonnull List<Long> granted, @Nonnull RoleEntity role) {
        List<RbacPrivilegeEntity> grantedList = rolePrivilegeMapper.listPrivilegesByRoleIds(List.of(role.getId()),
                role.getTenantId());
        val privileges = new ArrayList<RolePrivilegeEntity>();
        for (val pid : granted) {
            val entity = privilegeRepository.getById(pid);
            if (entity != null) {
                // 权限存在
                if (!rolePrivilegeMapper.existsByRoleIdAndPrivilegeId(role.getId(), entity.getId())) {
                    // 未授权
                    val rolePrivilege = new RolePrivilegeEntity();
                    rolePrivilege.setTenantId(entity.getTenantId());
                    rolePrivilege.setRoleId(role.getId());
                    rolePrivilege.setPrivilegeId(entity.getId());
                    privileges.add(rolePrivilege);
                }

                if (!grantedList.isEmpty()) {
                    // 从原有的授权列表中移除已经存在的授权
                    grantedList = grantedList.stream().filter((r) -> !r.getId().equals(pid)).toList();
                }
            }
        }

        if (!grantedList.isEmpty()) {
            // 删除不再分配的权限
            for (RbacPrivilegeEntity privilege : grantedList) {
                val deleted = rolePrivilegeMapper.deleteByRoleIdAndPrivilegeId(role.getId(), privilege.getId());
                log.trace("Granted permission({}) of Role({}) revoked: {}", privilege.getPermission(), role.getRole(),
                        deleted > 0);
            }
        }

        if (!privileges.isEmpty()) {
            rolePrivilegeMapper.insert(privileges);
        }
    }

    /**
     * 更新角色的子角色
     */
    private void updateRoleChildren(@Nonnull List<Long> childrenList, @Nonnull RoleEntity role) {
        val roleId = role.getId();
        if (childrenList.isEmpty()) {
            this.roleChildrenMapper.deleteByRoleId(roleId);
            return;
        }
        // 下级角色
        List<RoleEntity> children = roleChildrenMapper.listChildren(role.getId(), role.getTenantId());
        // 上级角色
        List<RoleEntity> parents = parents(role.getId());
        // 新的下级角色
        val newChildren = new ArrayList<RoleChildrenEntity>();
        for (Long childId : childrenList) {
            val child = getById(childId);
            if (child != null) {
                // 判断：下级角色不能是自己且不能是自己的上级角色
                if (child.equals(role) || parents.contains(child)) {
                    val error = new IllegalChildError(role.getName());
                    error.emit();
                }
                if (!roleChildrenMapper.existsByRoleIdAndChildId(roleId, child.getId())) {
                    // 新的下级角色
                    val rc = new RoleChildrenEntity();
                    rc.setTenantId(role.getTenantId());
                    rc.setRoleId(roleId);
                    rc.setChildId(childId);
                    newChildren.add(rc);
                }
                if (!children.isEmpty()) {
                    // 从原有的下级角色列表中移除已经存在的角色
                    children = children.stream().filter((r) -> !r.getId().equals(childId)).toList();
                }
            }
        }

        if (!children.isEmpty()) {
            // 删除不再包含的下级角色
            for (val oldChild : children) {
                val deleted = roleChildrenMapper.deleteByRoleIdAndChildId(roleId, oldChild.getId());
                log.debug("child({}) deleted: {}", oldChild, deleted > 0);
            }
        }

        if (!newChildren.isEmpty()) {
            roleChildrenMapper.insert(newChildren);
        }
    }

}

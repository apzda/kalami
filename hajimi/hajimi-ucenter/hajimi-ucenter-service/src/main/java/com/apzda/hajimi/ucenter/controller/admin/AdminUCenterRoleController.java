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
package com.apzda.hajimi.ucenter.controller.admin;

import com.apzda.hajimi.auditor.Audit;
import com.apzda.hajimi.ucenter.controller.admin.dto.RoleDto;
import com.apzda.hajimi.ucenter.controller.admin.dto.RolePageQuery;
import com.apzda.hajimi.ucenter.controller.admin.vo.RoleBaseInfoVo;
import com.apzda.hajimi.ucenter.domain.entity.RbacPrivilegeEntity;
import com.apzda.hajimi.ucenter.domain.entity.RoleEntity;
import com.apzda.hajimi.ucenter.domain.event.RoleDeletedEvent;
import com.apzda.hajimi.ucenter.domain.event.RoleModifiedEvent;
import com.apzda.hajimi.ucenter.domain.repository.RoleRepository;
import com.apzda.hajimi.ucenter.domain.vo.RoleVo;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.RoleChildrenMapper;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.RolePrivilegeMapper;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.tenant.TenantManager;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RestController
@RequestMapping("/wk/uc/role")
@RequiredArgsConstructor
public class AdminUCenterRoleController {

    private final UCenterConverter userConverter;

    private final RoleRepository roleRepository;

    private final RoleChildrenMapper roleChildrenMapper;

    private final RolePrivilegeMapper rolePrivilegeMapper;

    private final ApplicationEventPublisher publisher;

    /**
     * 角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("@authz.iCan('r:core.role')")
    public Response<Paged<RoleVo>> list(RolePageQuery query, QuickSearch quickSearch) {
        IPage<RoleEntity> roles = roleRepository.pageQuery(query, quickSearch);
        return Response.success(PageUtil.from(roles, userConverter::fromRoles));
    }

    /**
     * 获取角色的父、子角色和授权列表
     */
    @GetMapping("/info/{id}")
    @PreAuthorize("@authz.iCan('r:core.role')")
    public Response<RoleBaseInfoVo> info(@PathVariable String id) {
        val tenantId = TenantManager.tenantId("0");
        val role = roleRepository.getByTenantAndId(tenantId, id);
        if (role == null) {
            return Response.error(String.format("角色'%s'不存在", id));
        }

        val vo = new RoleBaseInfoVo();
        val children = roleChildrenMapper.listChildren(id, tenantId);
        if (!CollectionUtils.isEmpty(children)) {
            vo.setChildren(children.stream().map(RoleEntity::getId).toList());
        }

        val parents = roleRepository.parents(Long.parseLong(id));
        if (!CollectionUtils.isEmpty(parents)) {
            vo.setParents(parents.stream().map(RoleEntity::getId).toList());
        }

        val granted = rolePrivilegeMapper.listPrivilegesByRoleIds(List.of(id), tenantId);
        if (!CollectionUtils.isEmpty(granted)) {
            vo.setGranted(granted.stream().map(RbacPrivilegeEntity::getId).toList());
        }

        return Response.success(vo);
    }

    /**
     * 创建角色
     */
    @PostMapping("/create")
    @PreAuthorize("@authz.iCan('c:core.role')")
    @Audit(activity = "创建角色",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> create(@RequestBody @Validated({ Group.New.class, Group.Default.class }) RoleDto dto) {
        dto.setTenantId(TenantManager.tenantId("0"));

        if (roleRepository.create(dto)) {
            return Response.success(true).notify(String.format("角色'%s'创建成功", dto.getName()));
        }

        return Response.error(String.format("角色'%s'创建失败", dto.getName()));
    }

    /**
     * 更新角色
     */
    @PostMapping("/update")
    @PreAuthorize("@authz.iCan('u:core.role')")
    @Audit(activity = "修改角色", target = "#{#dto.id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> update(@RequestBody @Validated({ Group.Update.class, Group.Default.class }) RoleDto dto) {
        val tenantId = TenantManager.tenantId("0");
        val role = roleRepository.getByTenantAndId(tenantId, String.valueOf(dto.getId()));
        if (role == null) {
            return Response.error(String.format("角色'%d'不存在", dto.getId()));
        }

        dto.setTenantId(role.getTenantId());
        if (Boolean.TRUE.equals(role.getBuiltin())) {
            dto.setRole(null);
        }

        if (roleRepository.update(dto)) {
            publisher.publishEvent(new RoleModifiedEvent(role));
            return Response.success(true).notify(String.format("角色'%s'修改成功", dto.getName()));
        }
        return Response.error(String.format("角色'%s'修改失败", dto.getName()));

    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.sysUser() and @authz.iCan('d:core.role')")
    @Audit(activity = "删除角色", target = "#{#id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> delete(@PathVariable String id) {
        val tenantId = TenantManager.tenantId("0");
        val role = roleRepository.getByTenantAndId(tenantId, id);
        if (role == null) {
            return Response.error(String.format("角色'%s'不存在", id));
        }

        if (roleRepository.delete(role.getId())) {
            publisher.publishEvent(new RoleDeletedEvent(role));
            return Response.success(true).notify(String.format("角色'%s'已删除", role.getName()));
        }

        return Response.error("删除角色失败");
    }

}

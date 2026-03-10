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
import com.apzda.hajimi.auditor.AuditContextHolder;
import com.apzda.hajimi.ucenter.controller.admin.dto.PrivilegeDto;
import com.apzda.hajimi.ucenter.controller.admin.dto.PrivilegePageQuery;
import com.apzda.hajimi.ucenter.domain.entity.RbacPrivilegeEntity;
import com.apzda.hajimi.ucenter.domain.repository.PrivilegeRepository;
import com.apzda.hajimi.ucenter.domain.service.ResourceManager;
import com.apzda.hajimi.ucenter.domain.vo.PrivilegeVo;
import com.apzda.hajimi.ucenter.domain.vo.ResourceVo;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.kalami.annotation.Dictionary;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.tenant.TenantManager;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RestController
@RequestMapping("/wk/uc/privilege")
@RequiredArgsConstructor
public class AdminUCenterPrivilegeController {

    private final UCenterConverter userConverter;

    private final PrivilegeRepository privilegeRepository;

    private final ResourceManager resourceManager;

    /**
     * 权限列表
     */
    @GetMapping("/list")
    @PreAuthorize("@authz.iCan('r:core.privilege')")
    @Dictionary
    public Response<Paged<PrivilegeVo>> list(PrivilegePageQuery query, QuickSearch quickSearch) {
        IPage<RbacPrivilegeEntity> privileges = privilegeRepository.pageQuery(query, quickSearch);
        return Response.success(PageUtil.from(privileges, userConverter::fromPrivileges));
    }

    /**
     * 创建权限
     */
    @PostMapping("/create")
    @PreAuthorize("@authz.iCan('c:core.privilege')")
    @Audit(activity = "创建权限",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> create(
            @RequestBody @Validated({ Group.New.class, Group.Default.class }) PrivilegeDto dto) {
        dto.setTenantId(TenantManager.tenantId("0"));
        if (privilegeRepository.create(dto)) {
            return Response.success(true).notify(String.format("权限'%s'创建成功", dto.getName()));
        }
        else {
            return Response.error("创建权限失败");
        }
    }

    /**
     * 更新权限
     */
    @PostMapping("/update")
    @PreAuthorize("@authz.iCan('u:core.privilege')")
    @Audit(activity = "修改权限", target = "#{#dto.id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> update(
            @RequestBody @Validated({ Group.Update.class, Group.Default.class }) PrivilegeDto dto) {
        val privilege = privilegeRepository.getById(dto.getId());
        if (privilege == null) {
            return Response.error("权限不存在");
        }
        val tenantId = TenantManager.tenantId("0");
        if (!tenantId.equals(privilege.getTenantId())) {
            return Response.error("无权限更新");
        }

        dto.setTenantId(tenantId);
        dto.setType(null);

        AuditContextHolder.getContext().setNewValue(dto);

        if (privilegeRepository.update(dto)) {
            return Response.success(true).notify(String.format("权限'%s'修改成功", dto.getName()));
        }
        else {
            return Response.error("权限修改失败");
        }
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.iCan('d:core.privilege')")
    @Audit(activity = "删除权限", target = "#{#id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> delete(@PathVariable String id) {
        try {
            val privilege = privilegeRepository.getById(id);
            if (privilege == null) {
                return Response.error("权限不存在");
            }
            if (Boolean.TRUE.equals(privilege.getBuiltin())) {
                return Response.error("内置权限不能删除");
            }
            val tenantId = TenantManager.tenantId("0");
            if (!tenantId.equals(privilege.getTenantId())) {
                return Response.error("无权限删除");
            }
            if (privilegeRepository.delete(privilege)) {
                return Response.success(true).notify(String.format("权限'%s'已删除", privilege.getName()));
            }
            else {
                return Response.error("删除权限失败");
            }
        }
        catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

    /**
     * 全部资源
     */
    @GetMapping("/resources")
    @PreAuthorize("@authz.iCan('r:core.resource')")
    public Response<List<ResourceVo>> all() {
        val tenantId = TenantManager.tenantId("0");
        if ("0".equals(tenantId)) {
            return Response.success(resourceManager.listByPid("0"));
        }

        val subscriptions = TenantManager.subscriptions();

        return Response.success(resourceManager.mySubscribed(subscriptions.keySet().stream().toList()));
    }

}

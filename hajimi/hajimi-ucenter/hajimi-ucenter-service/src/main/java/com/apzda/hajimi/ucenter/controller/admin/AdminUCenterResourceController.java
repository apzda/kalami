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
import com.apzda.hajimi.ucenter.controller.admin.dto.ResourceDto;
import com.apzda.hajimi.ucenter.domain.entity.RbacResourceEntity;
import com.apzda.hajimi.ucenter.domain.repository.ResourceRepository;
import com.apzda.hajimi.ucenter.domain.service.ResourceManager;
import com.apzda.hajimi.ucenter.domain.vo.ResourceVo;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.kalami.annotation.Dictionary;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.tenant.Subscription;
import com.apzda.kalami.tenant.TenantManager;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RestController
@RequestMapping("/wk/uc/resource")
@RequiredArgsConstructor
public class AdminUCenterResourceController {

    private final UCenterConverter converter;

    private final ResourceRepository resourceRepository;

    private final ResourceManager resourceManager;

    /**
     * 资源列表
     */
    @GetMapping("/list/{pid}")
    @PreAuthorize("@authz.sysUser() and @authz.iCan('r:sys.resource')")
    @Dictionary
    public Response<List<ResourceVo>> list(@PathVariable String pid, QuickSearch quickSearch) {
        Map<String, String> param = new HashMap<>();
        if (quickSearch.isEnabled()) {
            quickSearch.getFields().forEach(field -> {
                param.put(field, quickSearch.getKeyword());
            });
        }
        List<RbacResourceEntity> resources = resourceRepository.listByPid(pid, param);

        return Response.success(converter.fromResources(resources));
    }

    /**
     * 系统可用订阅服务列表
     */
    @GetMapping("/services")
    @PreAuthorize("@authz.iCan('r:sys.resource')")
    public Response<Collection<Subscription>> availableServices() {
        val subscriptions = TenantManager.availableSubscriptions(true);

        return Response.success(subscriptions.values());
    }

    /**
     * 创建资源
     */
    @PostMapping("/create")
    @PreAuthorize("@authz.sysUser() and @authz.iCan('c:sys.resource')")
    @Audit(activity = "创建资源",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> create(@RequestBody @Validated({ Group.New.class, Group.Default.class }) ResourceDto dto) {
        val pid = dto.getPid();
        if (pid != 0) {
            val resource = resourceRepository.getById(pid);
            if (resource == null) {
                return Response.error(String.format("父资源'%d'不存在", pid));
            }
            dto.setService(resource.getService());
        }

        val service = dto.getService();
        val subscriptions = TenantManager.availableSubscriptions();
        if (!subscriptions.containsKey(service)) {
            return Response.error(String.format("来源'%s'不存在", service));
        }
        return Response.success(resourceRepository.create(dto)).notify(String.format("资源'%s'创建成功", dto.getName()));
    }

    /**
     * 更新资源
     */
    @PostMapping("/update")
    @PreAuthorize("@authz.sysUser() and @authz.iCan('u:sys.resource')")
    @Audit(activity = "修改资源", target = "#{#dto.id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> update(
            @RequestBody @Validated({ Group.Update.class, Group.Default.class }) ResourceDto dto) {
        val resource = resourceRepository.getById(dto.getId());
        if (resource == null) {
            return Response.error("资源不存在");
        }
        val service = dto.getService();
        val subscriptions = TenantManager.availableSubscriptions();
        if (!subscriptions.containsKey(service)) {
            return Response.error(String.format("来源'%s'不存在", service));
        }
        AuditContextHolder.getContext().setNewValue(dto);
        return Response.success(resourceRepository.update(dto)).notify(String.format("资源'%s'修改成功", dto.getName()));
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.sysUser() and @authz.iCan('d:sys.resource')")
    @Audit(activity = "删除资源", target = "#{#id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> delete(@PathVariable String id) {
        try {
            val resource = resourceRepository.getById(id);
            if (resource == null) {
                return Response.error("资源不存在");
            }
            resourceRepository.deleteById(id);
            return Response.success(true).notify(String.format("资源'%s'已删除", resource.getName()));
        }
        catch (Exception e) {
            return Response.error(e.getMessage());
        }
    }

}

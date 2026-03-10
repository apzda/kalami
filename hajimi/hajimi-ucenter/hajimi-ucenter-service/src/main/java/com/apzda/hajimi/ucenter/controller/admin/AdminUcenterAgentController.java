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
import com.apzda.hajimi.ucenter.controller.admin.dto.AgentPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.AgentEntity;
import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.hajimi.ucenter.domain.repository.AgentRepository;
import com.apzda.hajimi.ucenter.domain.vo.AgentVo;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.hajimi.ucenter.service.dto.AgentDto;
import com.apzda.kalami.annotation.Dictionary;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/wk/uc/agent")
@RequiredArgsConstructor
public class AdminUcenterAgentController {

    private final UCenterConverter userConverter;

    private final AgentRepository agentRepository;

    /**
     * 代理商列表
     */
    @GetMapping("/list")
    @PreAuthorize("@authz.notTenant() and @authz.iCan('r:sys.agent')")
    @Dictionary
    public Response<Paged<AgentVo>> list(AgentPageQuery query, QuickSearch quickSearch) {
        IPage<AgentEntity> agents = agentRepository.pageQuery(query, quickSearch);
        return Response.success(PageUtil.from(agents, userConverter::fromAgents));
    }

    /**
     * 代理商自动完成
     */
    @GetMapping("/autoComplete")
    @PreAuthorize("@authz.notTenant() and @authz.iCan('r:sys.agent')")
    public Response<List<AgentVo>> autoComplete(@RequestParam(required = false) String q,
            @RequestParam(required = false) String value) {
        val query = new AgentPageQuery();
        query.searchCount(false);
        query.setPageNumber(1);
        query.setPageSize(20);

        if (StringUtils.isNotBlank(value)) {
            query.setId(value);
        }
        else if (StringUtils.isNotBlank(q)) {
            query.setName(q);
        }

        IPage<AgentEntity> agents = agentRepository.pageQuery(query, new QuickSearch());
        agents.setRecords(agents.getRecords().stream().filter(entity -> entity.getId() != 0).toList());
        return Response.success(userConverter.fromAgents(agents.getRecords()));
    }

    /**
     * 创建代理商
     */
    @PostMapping("/create")
    @PreAuthorize("@authz.notTenant() and @authz.iCan('c:sys.agent')")
    @Audit(activity = "创建代理商", target = "#{#dto.name}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> create(@RequestBody @Validated({ Group.New.class, Group.Default.class }) AgentDto dto) {
        dto.setRatio(null);
        dto.setMerNo(null);
        if (agentRepository.create(dto) != null) {
            return Response.success(true).notify(String.format("代理商'%s'创建成功", dto.getName()));
        }
        else {
            return Response.error("创建代理商失败[DB]");
        }
    }

    /**
     * 更新代理商
     */
    @PostMapping("/update")
    @PreAuthorize("@authz.notTenant() and @authz.iCan('u:sys.agent')")
    @Audit(activity = "修改代理商", target = "#{#dto.id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> update(@RequestBody @Validated({ Group.Update.class, Group.Default.class }) AgentDto dto) {
        val agent = agentRepository.getById(dto.getId());
        if (agent == null) {
            return Response.error("代理商不存在");
        }
        dto.setRatio(null);
        dto.setMerNo(null);

        val context = AuditContextHolder.getContext();
        context.setOldValue(agent);
        context.setNewValue(dto);

        if (agentRepository.update(dto) != null) {
            return Response.success(true).notify(String.format("代理商'%s'修改成功", dto.getName()));
        }
        else {
            return Response.error("代理商修改失败[DB]");
        }
    }

    /**
     * 更新代理商
     */
    @PostMapping("/ratio")
    @PreAuthorize("@authz.notTenant() and @authz.iCan('rt:sys.agent')")
    @Audit(activity = "配置代理商费率", target = "#{#dto.id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> ratio(@RequestBody @Validated({ Group.Update.class, Group.Default.class }) AgentDto dto) {
        val agent = agentRepository.getById(dto.getId());
        if (agent == null) {
            return Response.error("代理商不存在");
        }
        val context = AuditContextHolder.getContext();
        context.setOldValue(agent.getRatio());
        context.setNewValue(dto.getRatio());

        val agentDto = new AgentDto();
        agentDto.setId(dto.getId());
        agentDto.setRatio(dto.getRatio());

        if (agentRepository.update(agentDto) != null) {
            return Response.success(true).notify(String.format("代理商'%s'费率配置成功", agent.getName()));
        }
        else {
            return Response.error("配置代理商费率失败[DB]");
        }
    }

    /**
     * 更新代理商
     */
    @PostMapping("/merchant")
    @PreAuthorize("@authz.notTenant() and @authz.iCan('m:sys.agent')")
    @Audit(activity = "配置代理商商户号", target = "#{#dto.id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> merchant(
            @RequestBody @Validated({ Group.Update.class, Group.Default.class }) AgentDto dto) {
        val agent = agentRepository.getById(dto.getId());
        if (agent == null) {
            return Response.error("代理商不存在");
        }
        val context = AuditContextHolder.getContext();
        context.setOldValue(agent.getMerNo());
        context.setNewValue(dto.getMerNo());

        val agentDto = new AgentDto();
        agentDto.setId(dto.getId());
        agentDto.setMerNo(dto.getMerNo());

        if (agentRepository.update(agentDto) != null) {
            return Response.success(true).notify(String.format("代理商'%s'商户号配置成功", agent.getName()));
        }
        else {
            return Response.error("配置代理商商户号失败[DB]");
        }
    }

    /**
     * 当前代理商信息
     */
    @GetMapping("/info/{id}")
    @PreAuthorize("@authz.iCan('r:sys.agent')")
    public Response<AgentVo> info(@PathVariable String id) {
        val agent = agentRepository.getById(id);
        if (agent == null) {
            return Response.error("代理商不存在");
        }

        return Response.success(userConverter.from(agent));
    }

    /**
     * 禁用代理商
     */
    @PostMapping("/disable/{id}")
    @PreAuthorize("@authz.notTenant() and @authz.iCan('u:sys.agent')")
    @Audit(activity = "禁用代理商", target = "#{#id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> disable(@PathVariable String id) {
        val agent = agentRepository.getById(id);
        if (agent == null) {
            return Response.error("代理商不存在");
        }

        agent.setStatus(TenantStatus.DISABLED);
        if (!agentRepository.updateById(agent)) {
            return Response.error("禁用代理商失败[DB]");
        }

        return Response.success(true).notify(String.format("代理商'%s'禁用成功", agent.getName()));
    }

    /**
     * 启用代理商
     */
    @PostMapping("/enable/{id}")
    @PreAuthorize("@authz.notTenant() and @authz.iCan('u:sys.agent')")
    @Audit(activity = "启用代理商", target = "#{#id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> enable(@PathVariable String id) {
        val tenant = agentRepository.getById(id);
        if (tenant == null) {
            return Response.error("代理商不存在");
        }
        tenant.setStatus(TenantStatus.ENABLED);
        if (!agentRepository.updateById(tenant)) {
            return Response.error("启用代理商失败[DB]");
        }

        return Response.success(true).notify(String.format("代理商'%s'启用成功", tenant.getName()));
    }

}

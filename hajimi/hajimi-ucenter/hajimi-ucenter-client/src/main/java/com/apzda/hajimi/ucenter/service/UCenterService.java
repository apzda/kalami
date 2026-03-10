/*
 * Copyright 2025-2026 the original author or authors.
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

package com.apzda.hajimi.ucenter.service;

import com.apzda.hajimi.ucenter.config.UCenterSecurityConfigureProperties;
import com.apzda.hajimi.ucenter.domain.enums.UserInfoScope;
import com.apzda.hajimi.ucenter.domain.vo.OAuthVo;
import com.apzda.hajimi.ucenter.domain.vo.RoleVo;
import com.apzda.hajimi.ucenter.domain.vo.UserVo;
import com.apzda.hajimi.ucenter.service.dto.*;
import com.apzda.hajimi.ucenter.tenant.DefaultSubscription;
import com.apzda.kalami.agent.Agent;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.tenant.Organization;
import com.apzda.kalami.tenant.Tenant;
import jakarta.annotation.Nonnull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
//@formatter:off
@FeignClient(
    name = "${kalami.cloud.feign.service.ucenter.name:ucenterSvc}",
    url = "${kalami.cloud.feign.service.ucenter.url:}",
    contextId = "kalami.cloud.feign.service.UCenterService",
    primary = false
)
//@formatter:on
public interface UCenterService {

    /**
     * 获取用户数据
     */
    @GetMapping(value = "/_/ucenterSvc/user/{id}")
    Optional<UserVo> getUser(@PathVariable Long id);

    /**
     * 获取租户用户
     * @param tenantId 租户ID
     * @param uid 用户ID
     */
    @GetMapping(value = "/_/ucenterSvc/user/{tenantId}/{uid}")
    Optional<UserVo> getTenantUserInfo(@PathVariable String tenantId, @PathVariable Long uid,
            @RequestParam(value = "scops", required = false) List<UserInfoScope> scops);

    /**
     * 获取部门用户ID
     * @param tenantId 租户ID
     * @param orgId 部门ID
     * @param uid 用户ID
     */
    @GetMapping(value = "/_/ucenterSvc/user/{tenantId}/{orgId}/{uid}")
    Optional<UserVo> getOrganizationUserInfo(@PathVariable String tenantId, @PathVariable String orgId,
            @PathVariable Long uid, @RequestParam(value = "scops", required = false) List<UserInfoScope> scops);

    /**
     * 创建用户
     */
    @PutMapping("/_/ucenterSvc/user")
    @Nonnull
    UserVo createUser(@RequestBody UserDto user);

    /**
     * 修改用户
     */
    @PatchMapping("/_/ucenterSvc/user")
    @Nonnull
    UserVo updateUser(@RequestBody UserDto user);

    /**
     * 获取OAuth
     */
    @GetMapping("/_/ucenterSvc/oauth")
    Optional<OAuthVo> getOauthByProviderAndAppAndUid(@RequestParam("provider") String provider,
            @RequestParam(value = "appId", required = false) String appId, @RequestParam("uid") String uid);

    /**
     * 用户授权列表
     * @param id 用户ID、租户ID和部门ID通过'@'连结组成,格式: userId@tenantId
     */
    @GetMapping(value = "/_/ucenterSvc/authorities/{id}")
    List<SimpleGrantedAuthority> getAuthority(@PathVariable String id);

    /**
     * 获取租户已订阅服务列表
     */
    @GetMapping("/_/ucenterSvc/subscription/{tenantId}")
    List<DefaultSubscription> getSubscriptions(@PathVariable String tenantId);

    /**
     * 租户订阅服务
     */
    @PostMapping("/_/ucenterSvc/tenant/subscribe")
    DefaultSubscription subscribe(@RequestBody @Validated SubscribeDto subscription);

    /**
     * 向用户中心同步安全相同配置
     */
    @PatchMapping("/_/ucenterSvc/security")
    Response<Boolean> sync(@RequestBody UCenterSecurityConfigureProperties properties);

    /**
     * 租户信息
     * @param id 租户ID
     */
    @GetMapping("/_/ucenterSvc/tenant/{id}")
    Optional<Tenant> getTenant(@PathVariable String id);

    /**
     * 代理信息
     * @param id 代理商ID
     */
    @GetMapping("/_/ucenterSvc/agent/{id}")
    Optional<Agent> getAgent(@PathVariable String id);

    /**
     * 代理信息
     * @param uid 代理商ID
     */
    @GetMapping("/_/ucenterSvc/getAgentByUid/{uid}")
    Optional<Agent> getAgentByUid(@PathVariable String uid);

    /**
     * 获取代理列表
     */
    @PostMapping("/_/ucenterSvc/getAgents")
    List<Agent> getAgents(@RequestBody AgentQuery query);

    /**
     * 查询代理列表
     */
    @GetMapping("/_/ucenterSvc/agents")
    Paged<Agent> agents(AgentQuery query);

    /**
     * 创建代理商
     */
    @PostMapping("/_/ucenterSvc/createAgent")
    Response<Boolean> createAgent(@RequestBody @Validated({ Group.Default.class, Group.New.class }) AgentDto agentDto);

    /**
     * 修改代理商
     */
    @PostMapping("/_/ucenterSvc/updateAgent")
    Response<Boolean> updateAgent(
            @RequestBody @Validated({ Group.Default.class, Group.Update.class }) AgentDto agentDto);

    /**
     * 用户所属租户列表
     * @param uid 用户ID
     */
    @GetMapping(value = "/_/ucenterSvc/tenants/{uid}")
    List<Tenant> getTenants(@PathVariable String uid);

    /**
     * 查询租户列表
     */
    @GetMapping(value = "/_/ucenterSvc/tenants", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Paged<Tenant> tenants(TenantRequest request);

    /**
     * 获取用户角色列表
     */
    @GetMapping("/_/ucenterSvc/listUserRoles/{tenantId}")
    List<RoleVo> listUserRolesByUsername(@PathVariable String tenantId, @RequestParam("username") String username);

    /**
     * 创建租户
     */
    @PostMapping("/_/ucenterSvc/tenant/create")
    Tenant createTenant(@RequestBody @Validated(Group.New.class) TenantDto dto);

    /**
     * 快速创建租户
     */
    @PostMapping("/_/ucenterSvc/tenant/create/v2")
    Tenant createTenant(@RequestBody @Validated @Nonnull SimpleTenantDto dto);

    /**
     * 修改租户
     */
    @PostMapping("/_/ucenterSvc/tenant/update")
    Tenant updateTenant(@RequestBody @Validated(Group.Update.class) TenantDto dto);

    /**
     * 部门信息
     * @param orgId 部门ID
     */
    @GetMapping("/_/ucenterSvc/organization/{orgId}")
    Optional<Organization> getOrganization(@PathVariable String orgId);

    /**
     * 用户所在部门列表
     * @param tenantId 租户ID
     * @param uid 用户ID
     */
    @GetMapping(value = "/_/ucenterSvc/organizations/{tenantId}/{uid}")
    List<Organization> getOrganizations(@PathVariable String tenantId, @PathVariable String uid);

    /**
     * 创建部门
     */
    @PostMapping("/_/ucenterSvc/organization/create")
    Organization createOrganization(@RequestBody @Validated(Group.New.class) OrganizationDto dto);

    /**
     * 修改部门
     */
    @PostMapping("/_/ucenterSvc/organization/update")
    Organization updateOrganization(@RequestBody @Validated(Group.Update.class) OrganizationDto dto);

    /**
     * 加入部门
     */
    @GetMapping("/_/ucenterSvc/organization/join/{tenantId}/{orgId}/{uid}")
    Response<Boolean> joinOrganization(@PathVariable Long tenantId, @PathVariable Long orgId, @PathVariable Long uid);

    /**
     * 离开部门
     */
    @GetMapping("/_/ucenterSvc/organization/leave/{tenantId}/{orgId}/{uid}")
    Response<Boolean> leaveOrganization(@PathVariable Long tenantId, @PathVariable Long orgId, @PathVariable Long uid);

    /**
     * 踢下线
     */
    @GetMapping("/_/ucenterSvc/user/kickoff/{id}")
    Response<Boolean> kickoff(@PathVariable String id);

    /**
     * 清空用户会话
     */
    @PostMapping(value = "/_/ucenterSvc/clearUserSession", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Response<Boolean> clearUserCache(@RequestParam("tid") String tenantId, @RequestParam("oid") String orgId,
            @RequestParam("uid") String username);

}

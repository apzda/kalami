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
package com.apzda.hajimi.ucenter.infrastructure.converter;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.apzda.hajimi.ucenter.domain.entity.*;
import com.apzda.hajimi.ucenter.domain.vo.*;
import com.apzda.hajimi.ucenter.service.vo.RefundVo;
import com.apzda.hajimi.ucenter.service.vo.TransactionVo;
import com.apzda.hajimi.ucenter.service.vo.TransitionVo;
import com.apzda.hajimi.ucenter.service.vo.UserAccountVo;
import com.apzda.kalami.agent.Agent;
import com.apzda.kalami.tenant.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UCenterConverter {

    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "orgId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "privileges", ignore = true)
    @Mapping(target = "metas", ignore = true)
    UserVo from(UserEntity user);

    List<UserVo> fromUsers(List<UserEntity> users);

    RoleVo from(RoleEntity role);

    List<RoleVo> fromRoles(List<RoleEntity> roles);

    PrivilegeVo from(RbacPrivilegeEntity privilege);

    List<PrivilegeVo> fromPrivileges(List<RbacPrivilegeEntity> privileges);

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "hasChildren", ignore = true)
    ResourceVo from(RbacResourceEntity resource);

    List<ResourceVo> fromResources(List<RbacResourceEntity> resources);

    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)
    @Mapping(target = "lastDevice", ignore = true)
    @Mapping(target = "lastIp", ignore = true)
    @Mapping(target = "lastUserAgent", ignore = true)
    OAuthVo from(OauthEntity oauth);

    List<OAuthVo> fromOAuths(List<OauthEntity> entities);

    OAuthSessionVo from(OauthSessionEntity session);

    List<OAuthSessionVo> fromSessions(List<OauthSessionEntity> sessions);

    @Mapping(target = "children", ignore = true)
    TenantVo from(TenantEntity tenant);

    List<TenantVo> fromTenants(List<TenantEntity> tenants);

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "pid", source = "parentId")
    @Mapping(target = "createTime", source = "createdAt")
    Tenant fromEntity(TenantEntity tenant);

    List<Tenant> toTenant(List<TenantEntity> tenants);

    @Mapping(target = "children", ignore = true)
    AgentVo from(AgentEntity agent);

    @Mapping(target = "children", ignore = true)
    @Mapping(target = "pid", source = "parentId")
    @Mapping(target = "ratios", source = "ratio")
    Agent fromEntity(AgentEntity agent);

    List<AgentVo> fromAgents(List<AgentEntity> agents);

    List<Agent> toAgent(List<AgentEntity> agents);

    UserAccountVo from(UserAccountEntity userAccount);

    TransactionVo from(UserAccountTransactionEntity transaction);

    List<TransactionVo> fromTransactions(List<UserAccountTransactionEntity> transactions);

    TransitionVo from(UserAccountTransitionEntity deduction);

    List<TransitionVo> fromDeductions(List<UserAccountTransitionEntity> deductions);

    RefundVo from(UserAccountRefundEntity refund);

    List<RefundVo> fromRefunds(List<UserAccountRefundEntity> refunds);

    default LocalDateTime fromEpochMilli(Long epochMilli) {
        if (epochMilli == null) {
            return null;
        }

        return LocalDateTimeUtil.of(epochMilli);
    }

}

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
package com.apzda.hajimi.ucenter.service.impl;

import com.apzda.hajimi.ucenter.service.UCenterService;
import com.apzda.hajimi.ucenter.user.DefaultUserInfo;
import com.apzda.kalami.agent.Agent;
import com.apzda.kalami.tenant.Organization;
import com.apzda.kalami.tenant.Tenant;
import com.apzda.kalami.user.UserInfo;
import com.apzda.kalami.user.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class UCenterUserInfoServiceImpl implements UserInfoService {

    private final UCenterService ucenterService;

    @Override
    public UserInfo getUserInfo(Serializable uid) {
        try {
            val resp = ucenterService.getUser(Long.parseLong(uid.toString()));
            if (resp.isPresent()) {
                val user = resp.get();
                val info = new DefaultUserInfo();
                info.setUid(uid);
                info.setUsername(user.getUsername());
                info.setDisplayName(user.getNickname());
                info.setEmail(user.getEmail());
                info.setPhone(user.getPhone());
                info.setAvatar(user.getAvatar());
                return info;
            }
        }
        catch (Exception e) {
            log.error("getUserInfo error: {} - }{}", uid, e.getMessage());
        }
        return null;
    }

    @Override
    public Tenant getTenant(Serializable tenantId) {
        try {
            val tenant = ucenterService.getTenant(tenantId.toString());
            if (tenant.isPresent()) {
                return tenant.get();
            }
        }
        catch (Exception e) {
            log.error("getTenant error: {} - }{}", tenantId, e.getMessage());
        }
        return null;
    }

    @Override
    public Organization getOrganization(Serializable orgId) {
        try {
            val organization = ucenterService.getOrganization(orgId.toString());
            if (organization.isPresent()) {
                return organization.get();
            }
        }
        catch (Exception e) {
            log.error("getOrganization error: {} - }{}", orgId, e.getMessage());
        }

        return null;
    }

    @Override
    public Agent getAgent(Serializable agentId) {
        try {
            val agent = ucenterService.getAgent(agentId.toString());
            if (agent.isPresent()) {
                return agent.get();
            }
        }
        catch (Exception e) {
            log.error("getAgent error: {} - }{}", agentId, e.getMessage());
        }

        return null;
    }

}

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

package com.apzda.hajimi.ucenter.infrastructure.mybatis.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.apzda.hajimi.ucenter.controller.admin.dto.AgentPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.AgentEntity;
import com.apzda.hajimi.ucenter.domain.entity.UserEntity;
import com.apzda.hajimi.ucenter.domain.enums.UserStatus;
import com.apzda.hajimi.ucenter.domain.repository.AgentRepository;
import com.apzda.hajimi.ucenter.domain.repository.RoleRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.AgentMapper;
import com.apzda.hajimi.ucenter.service.dto.AgentDto;
import com.apzda.hajimi.ucenter.service.dto.UserDto;
import com.apzda.kalami.agent.Agent;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.error.ResourceCreateError;
import com.apzda.kalami.error.ResourceNotFoundError;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.utils.TreeUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentRepositoryImpl extends ServiceImpl<AgentMapper, AgentEntity> implements AgentRepository {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    @Override
    public AgentEntity getByUid(String uid) {
        return lambdaQuery().eq(AgentEntity::getUid, uid).one();
    }

    @Override
    public IPage<AgentEntity> pageQuery(@Nonnull AgentPageQuery query, @Nonnull QuickSearch quickSearch) {
        IPage<AgentEntity> pager = PageUtil.from(query);
        if (quickSearch.isEnabled()) {
            query.setName(quickSearch.getKeyword());
        }

        return lambdaQuery().like(StringUtils.isNotBlank(query.getName()), AgentEntity::getName, query.getName())
            .eq(query.getParentId() != null, AgentEntity::getParentId, query.getParentId())
            .eq(query.getStatus() != null, AgentEntity::getStatus, query.getStatus())
            .eq(StringUtils.isNotBlank(query.getPhone()), AgentEntity::getPhone, query.getPhone())
            .eq(StringUtils.isNotBlank(query.getId()), AgentEntity::getId, query.getId())
            .eq(StringUtils.isNotBlank(query.getUid()), AgentEntity::getUid, query.getUid())
            .ge(query.getCreateStartTime() != null, AgentEntity::getCreatedAt, query.getCreateStartTime())
            .le(query.getCreateEndTime() != null, AgentEntity::getCreatedAt, query.getCreateEndTime())
            .page(pager);
    }

    @Override
    @Transactional
    @Nonnull
    public AgentEntity create(@Nonnull AgentDto dto) {
        val username = dto.getUsername();
        val password = dto.getPassword();
        val confirmPassword = dto.getConfirmPassword();

        if (!Strings.CS.equals(password, confirmPassword)) {
            log.error("【创建代理商失败】两次输入的密码不一致: {}", dto);
            throw new CommonBizException("两次输入的密码不一致");
        }

        // 上级代理商处理
        val parentId = Optional.ofNullable(dto.getParentId()).orElse(0L);
        if (parentId != 0) {
            val parent = this.getById(parentId);
            if (parent == null) {
                throw new CommonBizException(String.format("上级代理商%d不存在", parentId));
            }
            val pid = parent.getParentId();
            if (pid != null && pid != 0L) {
                throw new CommonBizException(String.format("【%s】不是一级代理", parent.getName()));
            }
        }
        // 检测手机号码
        if (this.phoneExist(dto.getPhone())) {
            throw new CommonBizException(String.format("手机号%s已被占用", dto.getPhone()));
        }
        // 创建代理商
        AgentEntity agent = new AgentEntity();
        agent.setParentId(parentId);
        agent.setTenantId("0");
        agent.setName(dto.getName());
        agent.setShortName(dto.getShortName());
        agent.setContact(dto.getContact());
        agent.setAddress(dto.getAddress());
        agent.setUid(0L);
        agent.setStatus(dto.getStatus());
        agent.setPhone(dto.getPhone());
        agent.setRemark(dto.getRemark());
        if (!this.save(agent)) {
            new ResourceCreateError("代理商").emit();
        }

        // 创建代理商管理员
        UserEntity user = userRepository.findByUsername(username);

        if (user == null) {
            val userDto = new UserDto();
            userDto.setTenantId("0"); // 代理商属于平台
            userDto.setUsername(username);
            userDto.setNickname(StringUtils.defaultIfBlank(dto.getNickname(), dto.getShortName()));
            userDto.setPassword(password);
            userDto.setConfirmPassword(confirmPassword);
            userDto.setPhone(dto.getPhone());
            userDto.setEnableSmsLogin(true);
            userDto.setStatus(UserStatus.ACTIVATED);
            userDto.setResetPassword(true);
            user = userRepository.create(userDto);
            if (user == null) {
                log.error("【创建代理商失败】创建代理商账户失败: {}", dto);
                throw new CommonBizException("创建代理商账户失败");
            }
        }

        userRepository.bindPhone(user.getId(), dto.getPhone());

        // 处理代理商账户角色
        val role = roleRepository.findByRoleAndTenantId("agent", "0");

        if (role == null) {
            throw new CommonBizException("代理商角色不存在");
        }
        userRepository.grantRoles(user.getId(), List.of(role));

        // 更新代理商超管
        agent.setUid(user.getId());
        if (!updateById(agent)) {
            throw new CommonBizException("创建代理商账户员失败");
        }

        return agent;
    }

    @Override
    @Transactional
    @Nonnull
    public AgentEntity update(@Nonnull AgentDto dto) {
        val agent = getById(dto.getId());
        if (agent == null) {
            throw new CommonBizException(new ResourceNotFoundError("代理商", String.valueOf(dto.getId())));
        }

        val phone = agent.getPhone();
        if (StringUtils.isNotBlank(dto.getPhone()) && !phone.equals(dto.getPhone())
                && this.phoneExist(dto.getPhone())) {
            throw new CommonBizException(String.format("手机号%s已被占用", dto.getPhone()));
        }

        val parentId = dto.getParentId();
        if (parentId != null) {
            if (parentId.equals(agent.getId())) {
                throw new CommonBizException("平级从属关系异常");
            }
            else {
                val parent = this.getById(parentId);
                if (parent == null) {
                    throw new CommonBizException(String.format("上级代理商%d不存在", parentId));
                }
                val pid = parent.getParentId();
                if (pid != null && pid != 0L) {
                    throw new CommonBizException(String.format("【%s】不是一级代理", parent.getName()));
                }
                val tenants = listChildren(String.valueOf(agent.getId()));
                if (TreeUtils.isMyChild(String.valueOf(parentId), tenants)) {
                    // 不是能自己下级的下级
                    throw new CommonBizException("上下级从属关系异常");
                }
            }
        }
        BeanUtil.copyProperties(dto, agent, CopyOptions.create().ignoreNullValue());

        if (this.updateById(agent)) {
            if (StringUtils.isNotBlank(dto.getPhone()) && !phone.equals(dto.getPhone())) {
                val userDto = new UserDto();
                userDto.setId(agent.getUid());
                userDto.setTenantId("0");
                userDto.setPhone(dto.getPhone());
                userDto.setEnableSmsLogin(true);
                userRepository.update(userDto);
                userRepository.grantRoles(agent.getUid(), List.of(roleRepository.findByRoleAndTenantId("agent", "0")));
            }

            return agent;
        }

        log.error("更新代理商失败[DB]： {} ", dto);

        throw new CommonBizException("更新代理商失败");
    }

    @Override
    public List<Agent> listChildren(String parentId) {
        if (StringUtils.isBlank(parentId) || "0".equals(parentId)) {
            return Collections.emptyList();
        }

        return lambdaQuery().eq(AgentEntity::getParentId, parentId).list().stream().map(agent -> {
            val id = String.valueOf(agent.getId());
            val t = new Agent();
            t.setId(id);
            t.setPid(String.valueOf(agent.getParentId()));
            t.setName(agent.getName());
            t.setPhone(agent.getPhone());
            t.setContact(agent.getContact());
            t.setRatios(agent.getRatio());
            t.setAddress(agent.getAddress());
            t.setShortName(agent.getShortName());
            t.setUid(agent.getUid().toString());
            t.setStatus(agent.getStatus().toString());
            t.setChildren(listChildren(id));
            return t;
        }).toList();
    }

    @Override
    public boolean phoneExist(String phone) {
        return this.lambdaQuery().eq(AgentEntity::getPhone, phone).exists();
    }

}

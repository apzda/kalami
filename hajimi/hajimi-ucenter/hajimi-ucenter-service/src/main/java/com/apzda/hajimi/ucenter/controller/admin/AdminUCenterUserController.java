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

package com.apzda.hajimi.ucenter.controller.admin;

import com.apzda.hajimi.auditor.Audit;
import com.apzda.hajimi.ucenter.controller.admin.dto.ResetPwdDto;
import com.apzda.hajimi.ucenter.controller.admin.dto.UserPageQuery;
import com.apzda.hajimi.ucenter.controller.common.UserCommonController;
import com.apzda.hajimi.ucenter.controller.common.dto.LoginReq;
import com.apzda.hajimi.ucenter.domain.entity.UserEntity;
import com.apzda.hajimi.ucenter.domain.vo.UserVo;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.hajimi.ucenter.security.authentication.token.UsernameAndPasswordToken;
import com.apzda.hajimi.ucenter.service.dto.UserDto;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.dto.IDS;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.error.ResourceNotFoundError;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.security.authentication.AuthenticationUtil;
import com.apzda.kalami.security.utils.SecurityUtils;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/23
 * @version 1.0.0
 */
@RestController
@RequestMapping("/wk/uc")
public class AdminUCenterUserController extends UserCommonController {

    private final UCenterConverter userConverter;

    public AdminUCenterUserController(ApplicationContext applicationContext) {
        super(applicationContext);
        this.userConverter = applicationContext.getBean(UCenterConverter.class);
    }

    /**
     * 用户名密码登录
     */
    @PostMapping("/login")
    public void login(@RequestBody LoginReq req, HttpServletRequest request, HttpServletResponse response) {
        val authentication = new UsernameAndPasswordToken(req.getUsername(), req.getPassword(), request);

        // 委派给AuthenticationManager
        AuthenticationUtil.authenticate(authentication, request, response);
    }

    /**
     * 用户列表
     * @param query 分页与查询
     * @param quickSearch 快速查询
     */
    @GetMapping("/user/list")
    @PreAuthorize("@authz.iCan('r:core.user')")
    public Response<Paged<UserVo>> list(UserPageQuery query, QuickSearch quickSearch) {
        if (quickSearch.isEnabled()) {
            query.setUsername(quickSearch.getKeyword());
        }

        IPage<UserEntity> users = repository.pageQuery(query);

        return Response.success(PageUtil.from(users, userConverter::fromUsers));
    }

    /**
     * 获取用户信息
     */
    @PostMapping("/user/info")
    public Response<List<UserVo>> info(@RequestBody IDS ids, CurrentUser currentUser) {
        if (CollectionUtils.isEmpty(ids.getIds())) {
            return Response.success(Collections.emptyList());
        }

        if (!SecurityUtils.hasPermission("r:core.user") || SecurityUtils.hasAuthority("TENANT")) {
            ids.getIds().clear();
            ids.getIds().add(currentUser.getUid());
        }
        val users = repository.list(Wrappers.lambdaQuery(UserEntity.class).in(UserEntity::getId, ids.getIds()));

        return Response.success(userConverter.fromUsers(users));
    }

    /**
     * 创建用户
     */
    @PostMapping("/user/create")
    @PreAuthorize("@authz.iCan('c:core.user')")
    @Audit(activity = "创建用户", succTpl = "创建成功: {}, 角色: {}", errorTpl = "创建失败: {}",
            args = { "#dto.username", "#dto.roles?.toString()" })
    public Response<Boolean> create(@Validated({ Group.New.class, Group.Default.class }) @RequestBody UserDto dto) {
        if (!Strings.CS.equals(dto.getPassword(), dto.getConfirmPassword())) {
            return Response.error("两次输入的密码不相同");
        }

        dto.setTenantId(TenantManager.tenantId("0"));

        repository.create(dto);

        return Response.success(true);
    }

    /**
     * 修改用户
     */
    @PostMapping("/user/update")
    @PreAuthorize("@authz.iCan('u:core.user')")
    @Audit(activity = "修改用户", target = "#{#dto.id}", succTpl = "修改成功: {}, 角色: {}, 状态: {}", errorTpl = "修改失败: {}",
            args = { "#dto.username", "#dto.roles?.toString()", "#dto.status?.name()" })
    public Response<Boolean> update(@Validated({ Group.Update.class, Group.Default.class }) @RequestBody UserDto dto,
            CurrentUser user) {
        if (!Strings.CS.equals(dto.getPassword(), dto.getConfirmPassword())) {
            return Response.error("两次输入的密码不相同");
        }
        dto.setTenantId(TenantManager.tenantId("0"));

        val uid = String.valueOf(dto.getId());
        if (uid.equals(user.getUid())) {
            // 可以修改自己的资料
            dto.setLanding(null);
            repository.updateBase(dto);
        }
        dto.setOrgId(null);
        repository.update(dto);

        return Response.success(true);
    }

    /**
     * 禁用用户
     */
    @PostMapping("/user/lock")
    @PreAuthorize("@authz.iCan('u:core.user')")
    @Audit(activity = "禁用用户", message = "成功", error = "失败")
    public Response<Boolean> lock(@RequestBody IDS ids) {
        val tenantId = TenantManager.tenantId("0");

        return Response
            .success(repository.lockUsers(tenantId, ids.getIds().stream().map(id -> (Serializable) id).toList()))
            .notify("用户已禁用");
    }

    /**
     * 启用用户
     */
    @PostMapping("/user/unlock")
    @PreAuthorize("@authz.iCan('u:core.user')")
    @Audit(activity = "启用用户", message = "成功", error = "失败")
    public Response<Boolean> unlock(@RequestBody IDS ids) {
        val tenantId = TenantManager.tenantId("0");
        return Response
            .success(repository.unlockUsers(tenantId, ids.getIds().stream().map(id -> (Serializable) id).toList()))
            .notify("用户已启用");
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/user/pwd/{uid}")
    @PreAuthorize("@authz.iCan('u:core.user')")
    @Audit(activity = "重置密码", target = "#{#uid}", message = "成功", error = "失败")
    public Response<Boolean> resetPassword(@PathVariable String uid, @RequestBody ResetPwdDto req) {
        val entity = repository.findByUserId(uid);
        if (entity == null) {
            return Response.error(new ResourceNotFoundError("用户", uid));
        }

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            return Response.error("确认密码与新密码不一致");
        }

        if (repository.lambdaUpdate()
            .set(UserEntity::getPasswd, passwordEncoder.encode(req.getPassword()))
            .set(UserEntity::getPasswdExpiredAt, 0L)
            .eq(UserEntity::getId, uid)
            .update()) {
            return Response.ok("密码重置成功");
        }

        return Response.error("修改密码失败");
    }

}

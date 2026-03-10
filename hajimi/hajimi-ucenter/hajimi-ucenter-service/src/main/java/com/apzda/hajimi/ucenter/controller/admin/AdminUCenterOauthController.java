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
import com.apzda.hajimi.ucenter.controller.admin.dto.OAuthSessionPageQuery;
import com.apzda.hajimi.ucenter.controller.admin.dto.OauthPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.OauthEntity;
import com.apzda.hajimi.ucenter.domain.repository.OauthRepository;
import com.apzda.hajimi.ucenter.domain.repository.OauthSessionRepository;
import com.apzda.hajimi.ucenter.domain.vo.OAuthSessionVo;
import com.apzda.hajimi.ucenter.domain.vo.OAuthVo;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.hajimi.ucenter.security.token.UCenterTokenManager;
import com.apzda.kalami.annotation.Dictionary;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.dto.IDS;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/wk/uc/oauth")
@RequiredArgsConstructor
public class AdminUCenterOauthController {

    private final OauthSessionRepository oauthSessionRepository;

    private final OauthRepository oauthRepository;

    private final UCenterConverter userConverter;

    private final UCenterTokenManager ucenterTokenManager;

    /**
     * 登录用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("@authz.sysUser() and @authz.iCan('r:sys.oauth')")
    @Dictionary
    public Response<Paged<OAuthVo>> pageQuery(OauthPageQuery req) {
        IPage<OAuthVo> result = oauthRepository.pageQuery(req);
        return Response.success(PageUtil.from(result, result.getRecords()));
    }

    /**
     * 登录会话列表
     */
    @GetMapping("/sessions")
    @PreAuthorize("@authz.sysUser() and @authz.iCan('r:sys.oauth.session')")
    @Dictionary
    public Response<Paged<OAuthSessionVo>> sessions(OAuthSessionPageQuery req) {
        val sessions = oauthSessionRepository.pageQuery(req);

        return Response.success(PageUtil.from(sessions, userConverter::fromSessions));
    }

    /**
     * 踢下线
     */
    @PostMapping("/kickoff")
    @PreAuthorize("@authz.sysUser() and @authz.iCan('k:sys.oauth.session')")
    @Audit(activity = "踢下线",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> kickoff(@RequestBody IDS dto) {
        val ids = dto.getIds();
        if (CollectionUtils.isEmpty(ids)) {
            return Response.error("未选择要踢下线的用户");
        }

        val logins = oauthRepository.lambdaQuery().in(OauthEntity::getId, ids).list();
        if (CollectionUtils.isEmpty(logins)) {
            return Response.error("要踢下线的用户不存在");
        }

        List<String> users = new ArrayList<>();
        logins.forEach(oauth -> {
            val provider = oauth.getProvider();
            val appId = oauth.getAppId();
            val uid = oauth.getUid();
            try {
                ucenterTokenManager.remove(String.valueOf(uid), provider, appId);
                log.info("将'{}({}@{})'踢下线成功", uid, provider, appId);
                users.add(String.valueOf(uid));
            }
            catch (Exception e) {
                log.warn("将'{}({}@{})'踢下线失败 - {}", uid, provider, appId, e.getMessage());
            }
        });

        if (!CollectionUtils.isEmpty(users)) {
            AuditContextHolder.getContext().setNewValue(users);
        }

        return Response.success(true).notify("所选用户已强制下线");
    }

}

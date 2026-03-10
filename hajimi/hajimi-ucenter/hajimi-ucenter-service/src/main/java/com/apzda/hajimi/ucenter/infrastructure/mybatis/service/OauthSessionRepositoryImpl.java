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
package com.apzda.hajimi.ucenter.infrastructure.mybatis.service;

import com.apzda.hajimi.ucenter.controller.admin.dto.OAuthSessionPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.OauthSessionEntity;
import com.apzda.hajimi.ucenter.domain.repository.OauthSessionRepository;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.OauthSessionEntityMapper;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
public class OauthSessionRepositoryImpl extends ServiceImpl<OauthSessionEntityMapper, OauthSessionEntity>
        implements OauthSessionRepository {

    @Override
    public IPage<OauthSessionEntity> pageQuery(OAuthSessionPageQuery req) {
        IPage<OauthSessionEntity> pager = PageUtil.from(req);

        return lambdaQuery().eq(StringUtils.isNotBlank(req.getUid()), OauthSessionEntity::getUid, req.getUid())
            .eq(StringUtils.isNotBlank(req.getOpenId()), OauthSessionEntity::getOpenId, req.getOpenId())
            .eq(StringUtils.isNotBlank(req.getUnionId()), OauthSessionEntity::getUnionId, req.getUnionId())
            .eq(StringUtils.isNotBlank(req.getProvider()), OauthSessionEntity::getProvider, req.getProvider())
            .eq(StringUtils.isNotBlank(req.getAppId()), OauthSessionEntity::getAppId, req.getAppId())
            .eq(StringUtils.isNotBlank(req.getDevice()), OauthSessionEntity::getDevice, req.getDevice())
            .eq(StringUtils.isNotBlank(req.getIp()), OauthSessionEntity::getIp, req.getIp())
            .likeRight(StringUtils.isNotBlank(req.getSpm()), OauthSessionEntity::getSpm, req.getSpm())
            .ge(req.getLoginStartTime() != null, OauthSessionEntity::getLoginTime, req.getLoginStartTime())
            .le(req.getLoginEndTime() != null, OauthSessionEntity::getLoginTime, req.getLoginEndTime())
            .page(pager);
    }

}

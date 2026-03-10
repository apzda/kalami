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

import com.apzda.hajimi.ucenter.config.UCenterConfigProperties;
import com.apzda.hajimi.ucenter.controller.admin.dto.OauthPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.OauthEntity;
import com.apzda.hajimi.ucenter.domain.enums.UserStatus;
import com.apzda.hajimi.ucenter.domain.repository.OauthRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.domain.vo.OAuthVo;
import com.apzda.hajimi.ucenter.event.OauthEvent;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.OauthEntityMapper;
import com.apzda.hajimi.ucenter.service.dto.OauthDto;
import com.apzda.hajimi.ucenter.service.dto.UserDto;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.sanitizer.PhoneNumberSanitizer;
import com.apzda.kalami.utils.NumUtils;
import com.apzda.kalami.utils.SnowflakeUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OauthRepositoryImpl extends ServiceImpl<OauthEntityMapper, OauthEntity> implements OauthRepository {

    private final UCenterConverter converter;

    private final ApplicationEventPublisher publisher;

    private final UserRepository userRepository;

    private final UCenterConfigProperties ucenterConfigProperties;

    @Override
    public OauthEntity getByProviderAndAppIdAndOpenId(String provider, String appId, String openid) {
        return lambdaQuery().eq(OauthEntity::getProvider, provider)
            .eq(OauthEntity::getAppId, appId)
            .eq(OauthEntity::getOpenId, openid)
            .one();
    }

    @Override
    public OauthEntity getByProviderAndAppIdAndUid(String provider, String appId, String uid) {
        return lambdaQuery().eq(OauthEntity::getProvider, provider)
            .eq(OauthEntity::getAppId, appId)
            .eq(OauthEntity::getUid, uid)
            .last("LIMIT 1")
            .one();
    }

    @Override
    @Nonnull
    public IPage<OAuthVo> pageQuery(OauthPageQuery req) {
        IPage<OAuthVo> pager = PageUtil.from(req);

        return this.baseMapper.pageQuery(pager, req);
    }

    @Override
    @Nonnull
    public List<OauthEntity> listByUid(Serializable uid) {
        return lambdaQuery().eq(OauthEntity::getUid, uid).list();
    }

    @Override
    @Nonnull
    public List<OauthEntity> listByProviderAndUnionId(String provider, String unionid) {
        return lambdaQuery().eq(OauthEntity::getProvider, provider).eq(OauthEntity::getUnionId, unionid).list();
    }

    @Override
    @Transactional
    @Nonnull
    public OauthEntity createOauth(@Validated @NotNull @Nonnull OauthDto dto) {
        val provider = dto.getProvider();
        val appId = dto.getAppId();
        val openid = dto.getOpenid();
        val unionid = dto.getUnionid();
        val phone = dto.getPhone();

        // 1. 看看是不是已经存在
        val oauth = getByProviderAndAppIdAndOpenId(provider, appId, openid);

        if (oauth != null) {
            if (StringUtils.isNotBlank(unionid) && !unionid.equals(oauth.getUnionId())) {
                // 1.1 更新UNION ID
                oauth.setUnionId(unionid);
                try {
                    updateById(oauth);
                }
                catch (Exception e) {
                    log.error("更新认证的UNION ID失败: {}\n{}", e.getMessage(), oauth);
                }
            }

            return oauth;
        }

        Long userId = null;
        if (StringUtils.isNotBlank(unionid)) {
            List<OauthEntity> entities = listByProviderAndUnionId(provider, unionid);
            if (!entities.isEmpty()) {
                userId = entities.get(0).getUid();
            }
        }

        if (userId == null && !"phone".equals(provider) && StringUtils.isNotBlank(phone)) {
            val phoneOauth = getByProviderAndAppIdAndOpenId("phone", "", phone);
            if (phoneOauth != null) {
                userId = phoneOauth.getUid();
            }
            else if (log.isDebugEnabled()) {
                log.debug("手机号还不是用户: {}", phone);
            }
        }

        if (userId == null) {
            val userDto = new UserDto();
            val id = NumUtils.toBase36(SnowflakeUtil.SNOWFLAKE.nextId());
            userDto.setUsername(String.format("%s%s", ucenterConfigProperties.getUserNamePrefix(), id));
            userDto.setStatus(UserStatus.ACTIVATED);
            if (StringUtils.isNotBlank(phone)) {
                userDto.setNickname(String.format("%s%s", ucenterConfigProperties.getNickNamePrefix(),
                        PhoneNumberSanitizer.sanitize(phone)));
            }
            else {
                userDto.setNickname(String.format("%s%s", ucenterConfigProperties.getNickNamePrefix(), id));
            }
            userDto.setPhone(phone);
            userDto.setEnableSmsLogin(true);
            val user = userRepository.create(userDto);

            if ("phone".equals(provider)) {
                return getByProviderAndAppIdAndOpenId(provider, appId, openid);
            }

            userId = user.getId();
        }

        if (userId == null) {
            log.error("创建用户失败：{}", dto);
            throw new CommonBizException("创建用户失败");
        }

        // 创建
        OauthEntity oauthEntity = new OauthEntity();

        oauthEntity.setUid(userId);
        oauthEntity.setProvider(provider);
        oauthEntity.setAppId(appId);
        oauthEntity.setOpenId(openid);
        oauthEntity.setUnionId(unionid);
        oauthEntity.setLoginTime(dto.getLoginTime());
        oauthEntity.setSpm(dto.getSpm());
        oauthEntity.setDevice(dto.getDevice());
        oauthEntity.setIp(dto.getIp());
        oauthEntity.setUserAgent(dto.getUserAgent());
        oauthEntity.setRemark(dto.getRemark());
        oauthEntity.setRecCode(dto.getRecCode());

        if (save(oauthEntity)) {
            val vo = converter.from(oauthEntity);
            vo.setPhone(phone);
            publisher.publishEvent(new OauthEvent(vo));
            return oauthEntity;
        }

        throw new CommonBizException("创建认证失败[DB]");
    }

}

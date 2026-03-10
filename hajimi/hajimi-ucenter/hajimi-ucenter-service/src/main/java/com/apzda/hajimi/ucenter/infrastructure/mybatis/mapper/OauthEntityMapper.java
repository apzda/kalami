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
package com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper;

import com.apzda.hajimi.ucenter.controller.admin.dto.OauthPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.OauthEntity;
import com.apzda.hajimi.ucenter.domain.vo.OAuthVo;
import com.apzda.hajimi.ucenter.token.UserToken;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface OauthEntityMapper extends BaseMapper<OauthEntity> {

    IPage<OAuthVo> pageQuery(IPage<OAuthVo> pager, @Param("q") OauthPageQuery q);

    @Select("SELECT * FROM ucenter_oauth WHERE uid = #{uid} AND deleted = false")
    List<OauthEntity> listByUid(@Param("uid") Serializable uid);

    OauthEntity getByAppAndProviderAndOpenId(@Param("app") String app, @Param("provider") String provider,
            @Param("openId") String openId);

    default OauthEntity getByProviderAndOpenId(String provider, String openId) {
        return getByAppAndProviderAndOpenId(UserToken.DEFAULT_APPID, provider, openId);
    }

    default OauthEntity getOpenId(String openId) {
        return getByAppAndProviderAndOpenId(UserToken.DEFAULT_APPID, UserToken.DEFAULT_PROVIDER, openId);
    }

}

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
package com.apzda.hajimi.openapi.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.hajimi.openapi.domain.OpenAppClient;
import com.apzda.hajimi.openapi.domain.OpenAppClientPerm;
import com.apzda.hajimi.openapi.domain.bo.OpenAppClientBo;
import com.apzda.hajimi.openapi.domain.vo.ClientPerm;
import com.apzda.hajimi.openapi.domain.vo.OpenAppClientVo;
import com.apzda.hajimi.openapi.mapper.OpenAppClientMapper;
import com.apzda.hajimi.openapi.mapper.OpenAppClientPermMapper;
import com.apzda.hajimi.openapi.service.IOpenAppClientService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 开放平台应用客户端Service业务层处理
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAppClientServiceImpl extends ServiceImpl<OpenAppClientMapper, OpenAppClient>
        implements IOpenAppClientService {

    private final OpenAppClientMapper baseMapper;

    private final OpenAppClientPermMapper permMapper;

    /**
     * 查询开放平台应用客户端
     * @param id 主键
     * @return 开放平台应用客户端
     */
    @Override
    public OpenAppClientVo queryById(Long id) {
        val openAppClient = baseMapper.selectById(id);
        return BeanUtil.toBean(openAppClient, OpenAppClientVo.class);
    }

    @Override
    @Cacheable(cacheNames = "open.app.client", key = "#clientId")
    public OpenAppClient getByClientId(String clientId) {
        return lambdaQuery().eq(OpenAppClient::getClientId, clientId).one();
    }

    private LambdaQueryWrapper<OpenAppClient> buildQueryWrapper(OpenAppClientBo bo) {
        LambdaQueryWrapper<OpenAppClient> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(OpenAppClient::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getClientId()), OpenAppClient::getClientId, bo.getClientId());
        lqw.eq(StringUtils.isNotBlank(bo.getClientName()), OpenAppClient::getClientName, bo.getClientName());
        lqw.eq(bo.getDisabled() != null, OpenAppClient::getDisabled, bo.getDisabled());

        return lqw;
    }

    /**
     * 新增开放平台应用客户端
     * @param bo 开放平台应用客户端
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(OpenAppClientBo bo) {
        OpenAppClient add = BeanUtil.toBean(bo, OpenAppClient.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改开放平台应用客户端
     * @param bo 开放平台应用客户端
     * @return 是否修改成功
     */
    @Override
    @CacheEvict(cacheNames = "open.app.client", key = "#bo.clientId")
    public Boolean updateByBo(OpenAppClientBo bo) {
        OpenAppClient update = BeanUtil.toBean(bo, OpenAppClient.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Cacheable(cacheNames = "open.app.perm", key = "#clientId")
    public OpenAppClientPerm getPermByClientId(String clientId) {
        return permMapper
            .selectOne(Wrappers.lambdaQuery(OpenAppClientPerm.class).eq(OpenAppClientPerm::getClientId, clientId));
    }

    @Override
    @CacheEvict(cacheNames = "open.app.perm", key = "#clientId")
    @Transactional(rollbackFor = Exception.class)
    public Boolean grantPermByClientId(String clientId, ClientPerm perm) {
        var p = permMapper
            .selectOne(Wrappers.lambdaQuery(OpenAppClientPerm.class).eq(OpenAppClientPerm::getClientId, clientId));
        if (p == null) {
            p = new OpenAppClientPerm();
            p.setClientId(clientId);
        }
        p.setPerm(perm);

        if (p.getId() != null) {
            return permMapper.updateById(p) >= 0;
        }
        else {
            return permMapper.insert(p) > 0;
        }
    }

    @Override
    @CacheEvict(cacheNames = "open.app.perm", key = "#clientId")
    public Boolean setClientIpWhitList(String clientId, String ipWhiteList) {
        var p = permMapper
            .selectOne(Wrappers.lambdaQuery(OpenAppClientPerm.class).eq(OpenAppClientPerm::getClientId, clientId));
        ClientPerm perm;
        if (p == null) {
            p = new OpenAppClientPerm();
            p.setClientId(clientId);
            perm = new ClientPerm();
        }
        else {
            perm = p.getPerm();
        }
        perm.setIpWhiteList(ipWhiteList);
        p.setPerm(perm);

        if (p.getId() != null) {
            return permMapper.updateById(p) >= 0;
        }
        else {
            return permMapper.insert(p) > 0;
        }
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(OpenAppClient entity) {

    }

}

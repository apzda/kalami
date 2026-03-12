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
package com.apzda.hajimi.openapi.service;

import com.apzda.hajimi.openapi.domain.OpenAppClient;
import com.apzda.hajimi.openapi.domain.OpenAppClientPerm;
import com.apzda.hajimi.openapi.domain.bo.OpenAppClientBo;
import com.apzda.hajimi.openapi.domain.vo.ClientPerm;
import com.apzda.hajimi.openapi.domain.vo.OpenAppClientVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 开放平台应用客户端Service接口
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 * @date 2026-03-06
 */
public interface IOpenAppClientService extends IService<OpenAppClient> {

    OpenAppClient getByClientId(String clientId);

    /**
     * 查询开放平台应用客户端
     * @param id 主键
     * @return 开放平台应用客户端
     */
    OpenAppClientVo queryById(Long id);

    /**
     * 新增开放平台应用客户端
     * @param bo 开放平台应用客户端
     * @return 是否新增成功
     */
    Boolean insertByBo(OpenAppClientBo bo);

    /**
     * 修改开放平台应用客户端
     * @param bo 开放平台应用客户端
     * @return 是否修改成功
     */
    Boolean updateByBo(OpenAppClientBo bo);

    /**
     * 获取客户端授权
     * @param clientId 客户端ID
     * @return 授权
     */
    OpenAppClientPerm getPermByClientId(String clientId);

    /**
     * 授权
     * @param clientId 客户ID
     * @param perm 授权
     * @return 是否成功
     */
    Boolean grantPermByClientId(String clientId, ClientPerm perm);

    /**
     * 设置Ip 白名称
     * @param clientId 客户端ID
     * @param ipWhiteList IP白名单
     * @return 是否成功
     */
    Boolean setClientIpWhitList(String clientId, String ipWhiteList);

}

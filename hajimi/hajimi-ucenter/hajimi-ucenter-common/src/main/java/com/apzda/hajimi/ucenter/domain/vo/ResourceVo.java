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
package com.apzda.hajimi.ucenter.domain.vo;

import com.apzda.kalami.dictionary.Dict;
import com.apzda.kalami.tenant.SubscriptionTransformer;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceVo {

    private String id;

    /**
     * 上级资源ID
     */
    private String pid;

    /**
     * 父级资源名称
     */
    private String parentName;

    /**
     * 父级资源ID
     */
    private String parentRid;

    /**
     * 资源标识
     */
    private String rid;

    /**
     * 所属可订阅服务
     */
    @Dict(transformer = SubscriptionTransformer.class)
    private String service;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 支持的操作列表(逗号分隔)
     */
    private String actions;

    /**
     * 内置资源
     */
    private Boolean builtin;

    /**
     * 资源ID浏览器类的全类名(FQDN)
     */
    private String explorer;

    /**
     * 资源说明
     */
    private String description;

    /**
     * 直接下级资源数量
     */
    private Integer childrenCount;

    private List<ResourceVo> children;

    private Boolean hasChildren;

    public Boolean getHasChildren() {
        if (childrenCount == null || childrenCount == 0) {
            return null;
        }

        return true;
    }

}

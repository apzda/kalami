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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.apzda.hajimi.ucenter.controller.admin.dto.ResourceDto;
import com.apzda.hajimi.ucenter.domain.entity.RbacResourceEntity;
import com.apzda.hajimi.ucenter.domain.repository.ResourceRepository;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.RbacResourceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nullable;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service
public class ResourceRepositoryImpl extends ServiceImpl<RbacResourceMapper, RbacResourceEntity>
        implements ResourceRepository {

    private static final Pattern PERM = Pattern.compile("^([^:]+):([^\\\\.]+).*$");

    @Override
    public List<RbacResourceEntity> listByPid(String pid, Map<String, String> param) {
        return this.baseMapper.listByPid(pid, param);
    }

    @Override
    public List<RbacResourceEntity> listByPidAndServices(String pid, List<String> subscriptions) {
        return this.baseMapper.listByPidAndServices(pid, subscriptions);
    }

    @Override
    public boolean create(ResourceDto dto) {
        RbacResourceEntity resource = new RbacResourceEntity();
        BeanUtil.copyProperties(dto, resource);
        resource.setId(null);
        return this.save(resource);
    }

    @Override
    public boolean update(ResourceDto dto) {
        RbacResourceEntity resource = new RbacResourceEntity();
        BeanUtil.copyProperties(dto, resource, CopyOptions.create().setIgnoreNullValue(true));
        return this.updateById(resource);
    }

    @Override
    public void deleteById(String id) {
        if (this.lambdaQuery().eq(RbacResourceEntity::getPid, id).count() > 0) {
            throw new IllegalStateException("拥有子资源，不可删除");
        }
        this.baseMapper.deleteResourceById(id);
    }

    @Override
    public RbacResourceEntity findByPidAndRid(Serializable pid, String rid) {
        return lambdaQuery().eq(RbacResourceEntity::getPid, pid).eq(RbacResourceEntity::getRid, rid).one();
    }

    @Override
    @Nullable
    public RbacResourceEntity findByPermission(String permission) {
        if (StringUtils.isBlank(permission)) {
            return null;
        }

        val matcher = PERM.matcher(permission);
        if (matcher.find()) {
            val rid = matcher.group(2);
            if (!"*".equals(rid)) {
                return findByPidAndRid("0", rid);
            }
        }

        return null;
    }

}

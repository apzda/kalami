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

import com.apzda.hajimi.ucenter.domain.entity.RbacResourceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface RbacResourceMapper extends BaseMapper<RbacResourceEntity> {

    List<RbacResourceEntity> listByPid(@Param("pid") String pid, @Param("param") Map<String, String> param);

    int deleteResourceById(@Param("id") String id);

    List<RbacResourceEntity> listByPidAndServices(@Param("pid") String pid,
            @Param("service") List<String> subscriptions);

}

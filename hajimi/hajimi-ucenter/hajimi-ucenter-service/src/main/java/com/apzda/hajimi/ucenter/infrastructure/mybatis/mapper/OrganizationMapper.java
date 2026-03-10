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

import com.apzda.hajimi.ucenter.controller.admin.dto.OrganizationUserPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.OrganizationEntity;
import com.apzda.hajimi.ucenter.domain.vo.OrganizationVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface OrganizationMapper extends BaseMapper<OrganizationEntity> {

    IPage<OrganizationVo> users(IPage<OrganizationVo> pager, @Param("q") OrganizationUserPageQuery query);

}

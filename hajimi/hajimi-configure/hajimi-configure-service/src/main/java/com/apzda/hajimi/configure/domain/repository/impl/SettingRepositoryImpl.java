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
package com.apzda.hajimi.configure.domain.repository.impl;

import com.apzda.hajimi.configure.domain.entity.SettingEntity;
import com.apzda.hajimi.configure.domain.mapper.SettingMapper;
import com.apzda.hajimi.configure.domain.repository.SettingRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import org.springframework.stereotype.Service;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service("sysSettingRepositoryImpl")
public class SettingRepositoryImpl extends ServiceImpl<SettingMapper, SettingEntity> implements SettingRepository {

    @Override
    public SettingEntity findBySettingKey(String settingKey) {
        return lambdaQuery().eq(SettingEntity::getSettingKey, settingKey).one();
    }

    @Override
    public int updateByIdAndVersion(String setting, Long id, Short version) {
        if (lambdaUpdate().set(SettingEntity::getSetting, setting)
            .eq(SettingEntity::getId, id)
            .eq(SettingEntity::getVersion, version)
            .update()) {
            return 1;
        }

        return 0;
    }

    @Override
    public boolean save(@Nonnull SettingEntity entity) {
        if (entity.getId() == null) {
            entity.setVersion((short) 0);
            return super.save(entity);
        }
        else {
            return updateByIdAndVersion(entity.getSetting(), entity.getId(), entity.getVersion()) == 1;
        }
    }

}

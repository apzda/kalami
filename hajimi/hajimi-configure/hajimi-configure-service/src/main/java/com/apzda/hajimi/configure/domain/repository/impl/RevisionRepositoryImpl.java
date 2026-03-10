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

import com.apzda.hajimi.configure.domain.entity.RevisionEntity;
import com.apzda.hajimi.configure.domain.mapper.RevisionMapper;
import com.apzda.hajimi.configure.domain.repository.RevisionRepository;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service("mashupRevisionRepositoryImpl")
public class RevisionRepositoryImpl extends ServiceImpl<RevisionMapper, RevisionEntity> implements RevisionRepository {

    @Override
    public Optional<RevisionEntity> findFirstBySettingKeyOrderByRevisionDesc(@Nonnull String settingKey) {
        return lambdaQuery().eq(RevisionEntity::getSettingKey, settingKey)
            .orderByDesc(RevisionEntity::getRevision)
            .last("LIMIT 1")
            .oneOpt();
    }

    @Override
    public List<RevisionEntity> findAllBySettingKeyOrderByRevisionDesc(String settingKey, Pageable page) {
        IPage<RevisionEntity> pager = PageUtil.from(page, false);
        return lambdaQuery().eq(RevisionEntity::getSettingKey, settingKey)
            .orderByDesc(RevisionEntity::getRevision)
            .list(pager);
    }

    @Override
    public Optional<RevisionEntity> findBySettingKeyAndRevision(String settingKey, Integer revision) {
        return lambdaQuery().eq(RevisionEntity::getSettingKey, settingKey)
            .eq(RevisionEntity::getRevision, revision)
            .oneOpt();
    }

    @Override
    public boolean save(@Nonnull RevisionEntity entity) {
        return super.save(entity);
    }

}

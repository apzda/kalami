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
package com.apzda.hajimi.configure.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.crypto.digest.DigestUtil;
import com.apzda.hajimi.configure.domain.entity.RevisionEntity;
import com.apzda.hajimi.configure.domain.entity.SettingEntity;
import com.apzda.hajimi.configure.domain.repository.RevisionRepository;
import com.apzda.hajimi.configure.domain.repository.SettingRepository;
import com.apzda.hajimi.configure.event.SettingChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Service("mashupSettingManager")
@RequiredArgsConstructor
@Slf4j
public class SettingManager {

    private final SettingRepository settingRepository;

    private final RevisionRepository revisionRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Cacheable(cacheNames = "etc.cfg", key = "{@utils.md5(#settingCls)}", condition = "{#result!=null}")
    public SettingEntity load(String settingCls) {
        return settingRepository.findBySettingKey(genSettingKey(settingCls));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean save(SettingEntity setting) {
        Assert.notNull(setting, "Setting must not be null");
        if (!StringUtils.hasText(setting.getSettingKey())) {
            setting.setSettingKey(genSettingKey(setting.getSettingCls()));
        }

        val settingKey = setting.getSettingKey();
        val oldSetting = settingRepository.findBySettingKey(settingKey);
        val lastRevision = revisionRepository.findFirstBySettingKeyOrderByRevisionDesc(settingKey);

        if (oldSetting != null) {
            val revision = BeanUtil.copyProperties(oldSetting, RevisionEntity.class, "id", "createdAt", "createdBy");
            revision.setRevision(lastRevision.orElseGet(() -> new RevisionEntity().setRevision(0)).getRevision() + 1);
            revisionRepository.save(revision);
            BeanUtil.copyProperties(setting, oldSetting,
                    CopyOptions.create().ignoreNullValue().setIgnoreProperties("id"));
            settingRepository.save(oldSetting);
            eventPublisher.publishEvent(new SettingChangedEvent(setting.getSettingCls()));
            return true;
        }
        else {
            if (settingRepository.save(setting)) {
                eventPublisher.publishEvent(new SettingChangedEvent(setting.getSettingCls()));
                return true;
            }
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public SettingEntity save(String settingCls, String setting) {
        val entity = new SettingEntity();
        entity.setSettingCls(settingCls).setSetting(setting);
        if (this.save(entity)) {
            return entity;
        }
        return null;
    }

    public boolean delete(String settingCls) {
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean restore(String settingCls) {
        return restore(settingCls, -1);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean restore(String settingCls, int revision) {
        Optional<RevisionEntity> rev;
        if (revision > 0) {
            rev = revisionRepository.findBySettingKeyAndRevision(genSettingKey(settingCls), revision);
        }
        else {
            rev = revisionRepository.findFirstBySettingKeyOrderByRevisionDesc(genSettingKey(settingCls));
        }
        if (rev.isPresent()) {
            val setting = rev.get();
            val restoredSetting = BeanUtil.copyProperties(setting, SettingEntity.class, "id", "createdAt", "createdBy");
            return save(restoredSetting);
        }
        return false;
    }

    public List<RevisionEntity> revisions(String settingCls, Pageable page) {
        return revisionRepository.findAllBySettingKeyOrderByRevisionDesc(genSettingKey(settingCls), page);
    }

    private static String genSettingKey(String settingCls) {
        return DigestUtil.md5Hex(settingCls);
    }

}

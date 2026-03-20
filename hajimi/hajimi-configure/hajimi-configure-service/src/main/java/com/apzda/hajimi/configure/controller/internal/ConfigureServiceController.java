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
package com.apzda.hajimi.configure.controller.internal;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.hajimi.configure.Revision;
import com.apzda.hajimi.configure.client.ConfigureService;
import com.apzda.hajimi.configure.client.dto.RestoreReq;
import com.apzda.hajimi.configure.client.dto.RevisionReq;
import com.apzda.hajimi.configure.client.dto.SaveReq;
import com.apzda.hajimi.configure.domain.SettingManager;
import com.apzda.hajimi.configure.domain.entity.SettingEntity;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.error.ServiceError;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Primary;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController("sysConfigureServiceController")
@RequiredArgsConstructor
@Primary
public class ConfigureServiceController implements ConfigureService {

    private final SettingManager settingManager;

    @Override
    @CacheEvict(cacheNames = "etc.cfg", key = "{@utils.md5(#request.clazz)}")
    public Response<Boolean> save(SaveReq request) {
        try {
            SettingEntity setting = settingManager.save(request.getClazz(), request.getSetting());
            if (setting == null) {
                log.error("Cannot save setting({})", request.getClazz());
                return Response.error(ServiceError.SERVICE_ERROR);
            }
            return Response.success(true);
        }
        catch (Exception e) {
            log.error("Cannot save setting({}) - {}", request.getClazz(), e.getMessage());
            return Response.error(ServiceError.SERVICE_ERROR);
        }
    }

    @Override
    public Response<List<Revision>> revisions(@RequestBody @Validated @Nonnull RevisionReq request) {
        val pageable = request.getPageable();
        val key = request.getSettingCls();
        val revisions = settingManager.revisions(key, pageable);
        if (!CollectionUtils.isEmpty(revisions)) {
            List<Revision> records = revisions.stream().map(revisionEntity -> {
                Revision revision = new Revision();
                BeanUtil.copyProperties(revisionEntity, revision);
                return revision;
            }).toList();

            return Response.success(records);
        }
        return Response.success(List.of());
    }

    @Override
    @CacheEvict(cacheNames = "etc.cfg", key = "{@utils.md5(#request.clazz)}")
    public Response<Boolean> restore(@RequestBody @Validated @Nonnull RestoreReq request) {
        val key = request.getClazz();
        val revision = request.getRevision();
        boolean restored;
        if (revision != null) {
            restored = settingManager.restore(request.getClazz(), request.getRevision());
        }
        else {
            restored = settingManager.restore(request.getClazz(), 0);
        }

        if (restored) {
            return Response.success(true);
        }
        else {
            return Response.error(500, String.format("Cannot restore Setting(%s) to revision %d", request.getClazz(),
                    request.getRevision()));
        }
    }

    @Override
    public Response<String> load(String settingCls) {
        SettingEntity setting = settingManager.load(settingCls);

        if (setting == null) {
            return Response.error(404, "Setting not found");
        }
        else {
            return Response.success(setting.getSetting());
        }
    }

}

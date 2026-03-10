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
package com.apzda.hajimi.configure.service.impl;

import com.apzda.hajimi.configure.Setting;
import com.apzda.hajimi.configure.client.ConfigureService;
import com.apzda.hajimi.configure.client.dto.SaveReq;
import com.apzda.hajimi.configure.exception.SettingUnavailableException;
import com.apzda.hajimi.configure.service.SettingService;
import com.apzda.kalami.exception.BizException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;

import java.util.concurrent.ExecutionException;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Slf4j
public class SettingServiceImpl extends CacheLoader<@NonNull String, @NonNull Setting> implements SettingService {

    private final ObjectMapper objectMapper;

    private final Lazy<ConfigureService> configServiceLoader;

    private final LoadingCache<@NonNull String, @NonNull Setting> cache;

    public SettingServiceImpl(ObjectMapper objectMapper, ApplicationContext context) {
        this.objectMapper = objectMapper;
        this.configServiceLoader = Lazy.of(() -> context.getBean(ConfigureService.class));
        this.cache = CacheBuilder.newBuilder().build(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Setting> T load(@Nonnull Class<T> tClass, @Nonnull String tenantId)
            throws SettingUnavailableException {
        val settingKey = Setting.genSettingKey(tClass, tenantId);
        try {
            log.trace("Loading Setting({})", settingKey);
            val setting = cache.get(settingKey);
            return (T) setting;
        }
        catch (ExecutionException e) {
            throw new SettingUnavailableException(settingKey, e);
        }
    }

    @Override
    @SuppressWarnings("all")
    public Setting load(@Nonnull String settingKey) throws Exception {
        val settingMeta = SettingService.getMeta(settingKey);
        val aClass = settingMeta.settingClass();
        if (aClass == null) {
            throw new ClassNotFoundException(settingMeta.clazz());
        }
        val configService = configServiceLoader.get();
        val res = configService.load(settingKey);
        val errCode = res.getErrCode();

        if (errCode == 404) {
            // 应用使用默认值
            val setting = BeanUtils.instantiateClass(aClass);
            return setting;
        }
        else if (errCode != 0) {
            log.error("Cannot load setting({}): {}", settingKey, res.getErrMsg());
            throw new SettingUnavailableException(settingKey, res.getErrMsg());
        }

        val setting = objectMapper.readValue(res.getData(), settingMeta.settingClass());
        log.debug("Setting({}) loaded from ConfigureService", settingKey);

        return setting;
    }

    @Override
    public void refresh(String settingCls) {
        val settingMeta = SettingService.getMeta(settingCls);
        val settingKey = settingMeta.getSettingCls();
        cache.invalidate(settingKey);
        log.debug("Setting({}) cache invalidated", settingKey);
    }

    @Override
    public Setting save(@Nonnull Setting setting, @Nonnull String tenantId) {
        val settingKey = Setting.genSettingKey(setting.getClass(), tenantId);
        cache.invalidate(settingKey);
        val configService = configServiceLoader.get();
        val req = new SaveReq();
        try {
            req.setClazz(settingKey);
            req.setSetting(objectMapper.writeValueAsString(setting));

            configService.save(req);
        }
        catch (Exception e) {
            throw new BizException("Cannot save setting: " + settingKey.getClass(), e);
        }

        return setting;
    }

}

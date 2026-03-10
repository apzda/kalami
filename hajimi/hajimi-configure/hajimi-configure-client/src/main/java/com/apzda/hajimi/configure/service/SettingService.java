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
package com.apzda.hajimi.configure.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.apzda.hajimi.configure.Setting;
import com.apzda.hajimi.configure.exception.SettingUnavailableException;
import com.apzda.kalami.tenant.TenantManager;
import com.google.common.base.Splitter;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
public interface SettingService {

    <T extends Setting> T load(@Nonnull Class<T> tClass, @Nonnull String tenantId) throws SettingUnavailableException;

    void refresh(String settingCls);

    default <T extends Setting> T load(@Nonnull Class<T> tClass) throws SettingUnavailableException {
        return load(tClass, TenantManager.tenantId("0"));
    }

    Setting save(@Nonnull Setting setting, @Nonnull String tenantId);

    default Setting save(@Nonnull Setting setting) {
        return save(setting, TenantManager.tenantId("0"));
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    static Meta getMeta(@Nonnull String settingCls) {
        val keys = Splitter.on("@").omitEmptyStrings().trimResults().splitToList(settingCls);
        var sClass = settingCls;
        String tenantId = "0";
        if (keys.size() > 1) {
            sClass = keys.get(0);
            tenantId = keys.get(1);
        }
        try {
            val aClass = Class.forName(sClass);
            if (Setting.class.isAssignableFrom(aClass)) {
                return new Meta((Class<? extends Setting>) aClass, sClass, tenantId);
            }
        }
        catch (ClassNotFoundException ignored) {
        }
        return new Meta(null, sClass, tenantId);
    }

    @Nonnull
    static Meta getMeta(@Nonnull Class<? extends Setting> tClass, String tenantId) {
        return new Meta(tClass, tClass.getCanonicalName(), tenantId);
    }

    @Nonnull
    static Meta getMeta(@Nonnull Class<? extends Setting> tClass) {
        return new Meta(tClass, tClass.getCanonicalName(), TenantManager.tenantId("0"));
    }

    record Meta(Class<? extends Setting> settingClass, String clazz, String tenantId) {
        @Nonnull
        public String getSettingCls() {
            return clazz + "@" + StringUtils.defaultIfBlank(tenantId, "0");
        }

        @Nonnull
        public String getSettingKey() {
            return DigestUtil.md5Hex(getSettingCls());
        }
    }

}

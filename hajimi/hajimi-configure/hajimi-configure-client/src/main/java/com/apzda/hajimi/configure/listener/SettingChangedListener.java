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
package com.apzda.hajimi.configure.listener;

import com.apzda.hajimi.configure.event.SettingChangedEvent;
import com.apzda.hajimi.configure.service.SettingService;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationListener;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@RequiredArgsConstructor
@Slf4j
public class SettingChangedListener implements ApplicationListener<SettingChangedEvent> {

    public static final String BEAN_NAME = "com.apzda.cloud.config.listener.SettingChangedListenerBean";

    private final SettingService settingService;

    @Override
    public void onApplicationEvent(@Nonnull SettingChangedEvent event) {
        val source = event.getSource();
        val settingKey = source.toString();
        settingService.refresh(settingKey);
        log.info("Setting({}) refreshed locally!", settingKey);
    }

}

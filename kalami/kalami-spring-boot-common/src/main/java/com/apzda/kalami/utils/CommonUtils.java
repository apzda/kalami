/*
 * Copyright 2025 the original author or authors.
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
package com.apzda.kalami.utils;

import com.apzda.kalami.service.ICommonService;
import com.apzda.kalami.service.SerialNumberGenerator;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */

public class CommonUtils implements ICommonService {

    private final Lazy<ObjectProvider<SerialNumberGenerator>> serialNumberServiceProvider;

    public CommonUtils(ApplicationContext applicationContext) {
        this.serialNumberServiceProvider = Lazy
            .of(() -> applicationContext.getBeanProvider(SerialNumberGenerator.class));
    }

    public String md5(String text) {
        return DigestUtils.md5Hex(text);
    }

    @Override
    public String sn(String prefix, String type, int length, String padding) {
        return this.serialNumberServiceProvider.get()
            .orderedStream()
            .filter((serialNumberService) -> serialNumberService.support(type))
            .findFirst()
            .map((sn) -> sn.generate(prefix, length, padding))
            .orElseThrow(
                    () -> new IllegalStateException(String.format("SerialNumber Generator for '%s' not found!", type)));

    }

}

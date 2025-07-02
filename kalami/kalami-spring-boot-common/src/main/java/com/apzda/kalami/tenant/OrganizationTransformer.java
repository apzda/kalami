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
package com.apzda.kalami.tenant;

import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.dictionary.Transformer;
import com.apzda.kalami.user.UidTransformer;
import com.apzda.kalami.user.UserInfoService;
import lombok.val;
import org.springframework.data.util.Lazy;

import java.io.Serializable;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class OrganizationTransformer implements Transformer<Serializable> {

    private final Lazy<UserInfoService> userInfoService;

    public OrganizationTransformer() {
        userInfoService = Lazy.of(() -> {
            try {
                return KalamiContextHolder.getBean(UserInfoService.class);
            }
            catch (Exception e) {
                return new UidTransformer.DefaultUserInfoService();
            }
        });
    }

    @Override
    public Object transform(Serializable value, boolean all) {
        val service = userInfoService.get();
        try {
            val vo = service.getOrganization(value);
            if (vo == null) {
                return null;
            }
            if (all) {
                return vo;
            }
            return vo.getShortName();
        }
        catch (Exception ignored) {
        }

        return null;
    }

}

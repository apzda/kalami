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
package com.apzda.hajimi.ucenter.utils;

import com.apzda.hajimi.ucenter.domain.entity.UserEntity;
import com.apzda.hajimi.ucenter.domain.enums.UserStatus;
import jakarta.annotation.Nonnull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public abstract class UserUtils {

    @Nonnull
    public static UserDetails createUserDetails(@Nonnull UserEntity user,
            List<? extends GrantedAuthority> authorities) {

        return new User(user.getId().toString(), user.getPasswd(), user.getStatus() == UserStatus.ACTIVATED,
                user.getExpiredAt() == null || user.getExpiredAt() > System.currentTimeMillis(),
                user.getPasswdExpiredAt() == null || user.getPasswdExpiredAt() > System.currentTimeMillis(),
                user.getStatus() != UserStatus.LOCKED, authorities);
    }

}

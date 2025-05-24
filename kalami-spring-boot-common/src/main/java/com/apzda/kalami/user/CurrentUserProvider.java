/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.user;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;

import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public abstract class CurrentUserProvider implements InitializingBean {

    private static final CurrentUser currentUser = CurrentUser.builder().build();

    private static CurrentUserProvider provider;

    @Override
    public void afterPropertiesSet() throws Exception {
        provider = this;
    }

    @Nonnull
    public static CurrentUser getCurrentUser() {
        if (provider != null) {
            val user = provider.currentUser();
            return user != null ? user : currentUser;
        }

        return currentUser;
    }

    @Nonnull
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(getCurrentUser().getUid());
    }

    @Nullable
    protected abstract CurrentUser currentUser();

}

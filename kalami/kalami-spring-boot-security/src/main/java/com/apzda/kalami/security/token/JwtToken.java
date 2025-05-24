/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.security.token;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import org.springframework.lang.Nullable;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface JwtToken {

    // MFA状态: 1,2位
    int MFA_FLAG = 0b00000011;

    // 凭证过期: 3位
    int CREDENTIALS_EXPIRED_FLAG = 0b00000100;

    // 是否绑定: 4位
    int BOUND_FLAG = 0b00001000;

    // 是否激活: 5位
    int ACTIVATED_FLAG = 0b00010000;

    // 是否绑定: 6位
    int LOCKED_FLAG = 0b00100000;

    // 是否过期: 7位
    int EXPIRED_FLAG = 0b01000000;

    // 是否禁用
    int ENABLED_FLAG = 0b10000000;

    void setUid(String uid);

    String getUid();

    default void setRunAs(String runAs) {
    }

    @Nullable
    default String getRunAs() {
        return null;
    }

    void setAccessToken(String accessToken);

    String getAccessToken();

    void setRefreshToken(String RefreshToken);

    String getRefreshToken();

    void setStatus(String status);

    @Nullable
    String getStatus();

    void setMfa(Integer mfa);

    Integer getMfa();

    void setLocked(Boolean locked);

    Boolean getLocked();

    void setActivated(Boolean activated);

    Boolean getActivated();

    void setBound(Boolean bound);

    Boolean getBound();

    void setCredentialsExpired(Boolean credentialsExpired);

    Boolean getCredentialsExpired();

    void setExpired(Boolean expired);

    Boolean getExpired();

    void setDisabled(Boolean enabled);

    Boolean getDisabled();

    default String getFlag() {
        int flag = 0;
        if (getMfa() != null && getMfa() > 0) {
            flag = (int) (getMfa() & MFA_FLAG);
        }
        if (Boolean.TRUE.equals(getCredentialsExpired())) {
            flag |= CREDENTIALS_EXPIRED_FLAG;
        }
        if (Boolean.TRUE.equals(getBound())) {
            flag |= BOUND_FLAG;
        }
        if (Boolean.TRUE.equals(getActivated())) {
            flag |= ACTIVATED_FLAG;
        }
        if (Boolean.TRUE.equals(getLocked())) {
            flag |= LOCKED_FLAG;
        }
        if (Boolean.TRUE.equals(getExpired())) {
            flag |= EXPIRED_FLAG;
        }
        if (Boolean.TRUE.equals(getDisabled())) {
            flag |= ENABLED_FLAG;
        }
        return String.valueOf(flag);
    }

    static void parseFlag(int flag, @Nonnull JwtToken jwtToken) {
        jwtToken.setMfa(flag & MFA_FLAG);
        jwtToken.setCredentialsExpired((flag & CREDENTIALS_EXPIRED_FLAG) != 0);
        jwtToken.setBound((flag & BOUND_FLAG) != 0);
        jwtToken.setActivated((flag & ACTIVATED_FLAG) != 0);
        jwtToken.setLocked((flag & LOCKED_FLAG) != 0);
        jwtToken.setExpired((flag & EXPIRED_FLAG) != 0);
        jwtToken.setDisabled((flag & ENABLED_FLAG) != 0);
    }

}

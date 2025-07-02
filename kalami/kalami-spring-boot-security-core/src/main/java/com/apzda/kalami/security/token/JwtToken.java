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

import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public interface JwtToken {

    // MFA状态: 1,2位
    int MFA_FLAG = 0b00000011;

    // 凭证过期: 3位
    int CREDENTIALS_EXPIRED_FLAG = 0b00000100;

    // 是否绑定: 4位
    int UNBOUND_FLAG = 0b00001000;

    // 是否激活: 5位
    int DEACTIVATED_FLAG = 0b00010000;

    // 是否绑定: 6位
    int LOCKED_FLAG = 0b00100000;

    // 是否过期: 7位
    int EXPIRED_FLAG = 0b01000000;

    // 是否禁用
    int ENABLED_FLAG = 0b10000000;

    void setUid(String uid);

    String getUid();

    default void setRunas(String runAs) {
    }

    @Nullable
    default String getRunas() {
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

    void setDeactivated(Boolean activated);

    Boolean getDeactivated();

    void setUnbound(Boolean bound);

    Boolean getUnbound();

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
        if (Boolean.TRUE.equals(getUnbound())) {
            flag |= UNBOUND_FLAG;
        }
        if (Boolean.TRUE.equals(getDeactivated())) {
            flag |= DEACTIVATED_FLAG;
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

    default String authId() {
        return authId("pc", null);
    }

    default String authId(String device) {
        return authId(device, null);
    }

    default String authId(String device, String extra) {
        return authId(getUid(), device, extra);
    }

    @Nonnull
    static String authId(String uid, String device, String extra) {
        Assert.hasText(uid, "uid must not be null or empty");
        device = StringUtils.defaultIfBlank(device, "pc");
        if (StringUtils.isBlank(extra)) {
            return "security.auth." + device + "." + uid;
        }
        else {
            return "security.auth." + device + "." + uid + "." + extra;
        }
    }

    static void parseFlag(int flag, @Nonnull JwtToken jwtToken) {
        jwtToken.setMfa(flag & MFA_FLAG);
        jwtToken.setCredentialsExpired((flag & CREDENTIALS_EXPIRED_FLAG) != 0);
        jwtToken.setUnbound((flag & UNBOUND_FLAG) != 0);
        jwtToken.setDeactivated((flag & DEACTIVATED_FLAG) != 0);
        jwtToken.setLocked((flag & LOCKED_FLAG) != 0);
        jwtToken.setExpired((flag & EXPIRED_FLAG) != 0);
        jwtToken.setDisabled((flag & ENABLED_FLAG) != 0);
    }

}

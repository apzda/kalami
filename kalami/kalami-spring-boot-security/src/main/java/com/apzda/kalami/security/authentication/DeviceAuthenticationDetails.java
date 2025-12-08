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
package com.apzda.kalami.security.authentication;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Getter
public class DeviceAuthenticationDetails implements AuthenticationDetails {

    private final String app;

    private final String device;

    private final Map<String, String> meta;

    private final String remoteAddress;

    private final String userAgent;

    @Setter
    private String accessToken;

    @Setter
    private String refreshToken;

    @Setter
    private String spm;

    @Setter
    private Long expiredAt;

    public DeviceAuthenticationDetails(@Nonnull HttpHeaders headers, String remoteAddress) {
        this.userAgent = headers.getFirst("User-Agent");
        this.app = StringUtils.defaultIfBlank(headers.getFirst("X-App"), "");
        this.device = StringUtils.defaultIfBlank(headers.getFirst("X-Device"), "");
        this.remoteAddress = remoteAddress;
        this.meta = parseMeta(headers);
    }

    @Nonnull
    public static DeviceAuthenticationDetails create(@Nonnull HttpHeaders headers, String remoteAddress) {
        return new DeviceAuthenticationDetails(headers, remoteAddress);
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("remoteAddress", getRemoteAddress())
            .append("app", app)
            .append("device", device)
            .append("userAgent", userAgent)
            .append("expiredAt", expiredAt)
            .append("meta", meta)
            .toString();
    }

    @Nonnull
    private Map<String, String> parseMeta(@Nonnull HttpHeaders headers) {
        Map<String, String> meta = new MetaData();
        headers.forEach((key, value) -> {
            if (key.toLowerCase().startsWith("x-m-")) {
                meta.put(key.toLowerCase().substring(4), value.get(0));
            }
        });

        return meta;
    }

    public String getDevice() {
        return StringUtils.defaultIfBlank(device, "pc");
    }

    static class MetaData extends HashMap<String, String> {

        @Override
        public String get(Object key) {
            return super.get(((String) key).toLowerCase());
        }

        @Override
        public String getOrDefault(Object key, @Nullable String defaultValue) {
            return super.getOrDefault(((String) key).toLowerCase(), defaultValue);
        }

        @Override
        public boolean containsKey(Object key) {
            return super.containsKey(((String) key).toLowerCase());
        }

        @Override
        public String remove(Object key) {
            return super.remove(((String) key).toLowerCase());
        }

        @Override
        public String toString() {
            return super.toString();
        }

    }

}

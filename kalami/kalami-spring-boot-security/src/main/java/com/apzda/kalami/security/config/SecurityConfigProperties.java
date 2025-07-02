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
package com.apzda.kalami.security.config;

import com.apzda.kalami.security.token.JwtToken;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.boot.web.server.Cookie;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Data
@ConfigurationProperties("kalami.security")
public class SecurityConfigProperties {

    private boolean enabled = true;

    private boolean feignEnabled = false;

    private boolean traceEnabled;

    private String rolePrefix = "ROLE_";

    private CookieConfig cookie = new CookieConfig();

    private String argName;

    private String tokenName = "Authorization";

    private String bearer = "Bearer";

    private String jwtKey;

    @DurationUnit(ChronoUnit.SECONDS)
    private Duration jwtLeeway = Duration.ofSeconds(30);

    private String realmName = "Kalami";

    private String homeUrl = "/";

    private String loginUrl;

    private String logoutUrl;

    private Map<String, CorsConfig> cors = new HashMap<>();

    private List<String> excludes = new ArrayList<>();

    private List<Checker> checker = new ArrayList<>();

    private HeadersConfig headers;

    @DurationUnit(ChronoUnit.MINUTES)
    private Duration accessTokenTimeout = Duration.ofMinutes(30);

    @DurationUnit(ChronoUnit.DAYS)
    private Duration refreshTokenTimeout = Duration.ofDays(365);

    private Map<String, TokenConfig> app = new LinkedHashMap<>();

    public String getTokenName() {
        return StringUtils.defaultIfBlank(tokenName, "Authorization");
    }

    @Data
    public static class CookieConfig {

        private String cookieName;

        private String cookieDomain;

        private boolean cookieSecurity;

        private String cookiePath = "/";

        private Cookie.SameSite sameSite = Cookie.SameSite.STRICT;

        private int maxAge = -1;

        @Nonnull
        public jakarta.servlet.http.Cookie createCookie(@Nonnull JwtToken jwtToken) {
            val cookieName = getCookieName();
            val accessToken = jwtToken.getAccessToken();
            val cookie = new jakarta.servlet.http.Cookie(cookieName, accessToken);
            cookie.setDomain(this.getCookieDomain());
            cookie.setHttpOnly(true);
            cookie.setSecure(this.isCookieSecurity());
            cookie.setPath(this.getCookiePath());
            cookie.setMaxAge(this.getMaxAge());
            cookie.setAttribute("SameSite", this.getSameSite().attributeValue());
            return cookie;
        }

    }

    @Data
    public static class CorsConfig {

        private List<String> headers;

        private List<String> exposed;

        private Boolean credentials;

        private List<String> origins;

        private List<String> originPatterns;

        private Boolean allowPrivateNetwork;

        private List<String> methods;

        private Duration maxAge;

    }

    @Data
    public static class HeadersConfig {

        private boolean hsts = false;

        private boolean xss = false;

        private boolean frame = false;

        private boolean contentType = false;

    }

    @Data
    @Validated
    public static class Checker {

        @NotBlank
        private String path;

        private List<CheckerConfig> checkers = new ArrayList<>();

    }

    @Data
    @Validated
    public static class CheckerConfig {

        @NotBlank
        private String name;

        private boolean enabled = true;

        private Map<String, Object> args = new LinkedHashMap<>();

    }

    @Data
    public static class TokenConfig {

        /**
         * 访问令牌过期时间
         */
        @DurationUnit(ChronoUnit.MINUTES)
        private Duration accessTokenTimeout = Duration.ofMinutes(5);

        /**
         * 刷新令牌过期时间
         */
        @DurationUnit(ChronoUnit.DAYS)
        private Duration refreshTokenTimeout = Duration.ofDays(180);

        /**
         * 是否强制多因素认证
         */
        private boolean mfaEnabled = false;

    }

}

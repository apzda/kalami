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
package com.apzda.hajimi.ucenter.wx.mini.oauth;

import com.apzda.hajimi.ucenter.oauth.OauthResolver;
import com.apzda.hajimi.weixin.ma.WxMaConfigRegistration;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

import java.util.regex.Pattern;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@RequiredArgsConstructor
public class WxMiniOauthResolver implements OauthResolver {

    private final static Pattern pattern = Pattern.compile("^(.+)-1\\d{10}");

    private final ObjectProvider<WxMaConfigRegistration> wxMaConfigRegistrations;

    @Override
    public boolean supports(String provider) {
        return "wx".equals(provider);
    }

    @Override
    public String resolveAppId(String appId) {
        String resolved = null;
        for (WxMaConfigRegistration registration : wxMaConfigRegistrations.orderedStream().toList()) {
            val config = registration.loadConfig(appId);
            if (config != null) {
                resolved = config.getAppid();
                break;
            }
        }

        return resolved;
    }

    @Override
    public String resolveOpenId(String openId) {
        if (openId != null) {
            val matcher = pattern.matcher(openId);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return openId;
        }
        return null;
    }

    @Override
    public String resolveUnionId(String unionId) {

        if (unionId != null) {
            val matcher = pattern.matcher(unionId);
            if (matcher.matches()) {
                return matcher.group(1);
            }
            return unionId;
        }

        return null;
    }

}

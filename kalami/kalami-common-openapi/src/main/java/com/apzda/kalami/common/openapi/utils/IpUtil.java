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
package com.apzda.kalami.common.openapi.utils;

import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.text.StrSplitter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class IpUtil {

    public static Optional<String> getClientIpAddress() {
        val requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes sr) {
            HttpServletRequest request = sr.getRequest();

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            // 对于多级代理，X-Forwarded-For的第一个IP才是真实IP
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return Optional.ofNullable(ip);
        }

        return Optional.empty();
    }

    public static boolean match(String ipList, boolean emptyIsMatch) {
        if (StringUtils.isEmpty(ipList)) {
            return false;
        }

        val clientIpAddress = getClientIpAddress();
        if (clientIpAddress.isPresent()) {
            val ip = clientIpAddress.get();
            for (String beCheckedIp : StrSplitter.splitTrim(ipList, ",", true)) {
                beCheckedIp = beCheckedIp.trim();
                if (beCheckedIp.contains("/") && Ipv4Util.list(beCheckedIp, true).contains(ip)) {
                    return true;
                } else if (beCheckedIp.equals(ip)) {
                    return true;
                }
            }
        }

        return emptyIsMatch;
    }
}

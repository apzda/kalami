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
package com.apzda.hajimi.ucenter.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.Authentication;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public class OauthLoginEvent extends ApplicationEvent {

    @Getter
    private final Authentication authentication;

    public OauthLoginEvent(Event source, Authentication authentication) {
        super(source);
        this.authentication = authentication;
    }

    public Event getEvent() {
        return (Event) super.getSource();
    }

    @Data
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Event {

        /**
         * 认证提供器
         */
        private String provider;

        /**
         * AppId
         */
        private String appId;

        /**
         * OpenId
         */
        private String openId;

        /**
         * UnionId
         */
        private String unionId;

        /**
         * 平台用户ID
         */
        private String uid;

        /**
         * 认证ID
         */
        private String oauthId;

        /**
         * 部门ID
         */
        private String orgId;

        /**
         * 推荐码
         */
        private String recCode;

        /**
         * 手机号
         */
        private String phone;

        /**
         * SPM
         */
        private String spm;

        /**
         * IP
         */
        private String ip;

        /**
         * 设备
         */
        private String device;

        /**
         * 代理
         */
        private String userAgent;

    }

}

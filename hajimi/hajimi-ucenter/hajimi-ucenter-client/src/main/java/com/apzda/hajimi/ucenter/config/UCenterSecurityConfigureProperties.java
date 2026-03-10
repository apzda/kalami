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
package com.apzda.hajimi.ucenter.config;

import com.apzda.hajimi.ucenter.domain.enums.PrivilegeType;
import com.apzda.hajimi.ucenter.resource.ResourceExplorer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "hajimi.ucenter.security")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UCenterSecurityConfigureProperties {

    @JsonIgnore
    private boolean autoSync = false;

    private Map<String, Resource> resources = new HashMap<>();

    private Map<String, Role> roles = new HashMap<>();

    private Map<String, Privilege> privileges = new HashMap<>();

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Resource {

        /**
         * 资源名称
         */
        private String name;

        /**
         * 资源所属可订阅服务
         */
        private String service;

        /**
         * 资源支持的操作
         */
        private List<String> actions;

        /**
         * 说明
         */
        private String description;

        /**
         * 资源浏览器
         */
        private Class<? extends ResourceExplorer> explorer;

        /**
         * 子资源
         */
        private Map<String, Resource> children = new HashMap<>();

        /**
         * 权限列表
         */
        private Map<String, Privilege> privileges = new HashMap<>();

    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Role {

        /**
         * 角色名称
         */
        @NotBlank
        private String name;

        /**
         * 说明
         */
        private String description;

        /**
         * 租户的
         */
        private boolean leasable;

        /**
         * 关联的权限
         */
        private List<String> privileges;

    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Privilege {

        /**
         * 权限名称
         */
        @NotBlank
        private String name;

        /**
         * 权限ID
         */
        @NotNull
        private PrivilegeType type = PrivilegeType.resource;

        /**
         * 所属可订阅的服务
         */
        private String service;

        /**
         * 说明
         */
        private String description;

        /**
         * 额外配置
         */
        private String extra;

    }

}

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
package com.apzda.hajimi.ucenter.resource;

import lombok.Data;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
public interface ResourceExplorer {

    /**
     * 加载资源
     * @param selected 已经选择的资源ID列表
     */
    List<Resource> load(List<String> selected);

    @Data
    class Resource {

        /**
         * 资源ID
         */
        private String id;

        /**
         * 资源名称
         */
        private String name;

        /**
         * 是否已选择
         */
        private boolean checked;

        /**
         * 子资源
         */
        public List<Resource> children;

    }

}

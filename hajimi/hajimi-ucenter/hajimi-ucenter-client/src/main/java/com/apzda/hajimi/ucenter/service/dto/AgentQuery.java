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
package com.apzda.hajimi.ucenter.service.dto;

import com.apzda.kalami.data.AbstractPageQuery;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentQuery extends AbstractPageQuery {

    /**
     * 代理ID
     */
    private String id;

    /**
     * 上级ID
     */
    private Long parentId;

    /**
     * 代理名称
     */
    private String name;

    /**
     * 系统用户ID
     */
    private String uid;

    /**
     * 代理手机号
     */
    private String phone;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间开始
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createStartTime;

    /**
     * 创建时间结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createEndTime;

}

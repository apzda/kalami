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
package com.apzda.hajimi.supervisor.domain.entity;

import com.apzda.hajimi.supervisor.domain.vo.TaskStatus;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@TableName(value = "sys_supervisor_task")
@Data
public class SupervisorTask {

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 分片值
     */
    private Integer sharding;

    /**
     * 运行时间
     */
    private Long runAt;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 监督者
     */
    private String supervisor;

    /**
     * 状态
     */
    private TaskStatus status;

    /**
     * 任务内容
     */
    private String content;

    /**
     * 重试次数
     */
    private Integer retries;

    /**
     * 说明
     */
    private String remark;

}

/*
 * Copyright 2025 the original author or authors.
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

/*
 * This file is part of apzda created at 2023/7/7 by ningGf.
 */
package com.apzda.kalami.boot.entity;

import com.apzda.kalami.boot.transformer.Upper;
import com.apzda.kalami.data.domain.IEntity;
import com.apzda.kalami.dictionary.Dict;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created at 2023/7/7 13:18.
 *
 * @author ningGf
 * @version 1.0.0
 * @since 1.0.0
 **/
@Data
@TableName(value = "t_users", autoResultMap = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User implements IEntity<String> {

    @TableId(type = IdType.ASSIGN_ID, value = "uid")
    private String id;

    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private Long createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;

    @TableField(fill = FieldFill.INSERT)
    private String merchantId;

    @Dict(transformer = Upper.class)
    private String name;

    @Dict(transformer = Upper.class)
    @TableField(exist = false)
    private String name1 = "u1";

    @Version
    private Long ver;

    @TableLogic
    private Integer del;

    @Dict(entity = Role.class, code = "rid", value = "name")
    private String roles;

    @Dict(code = "test")
    private String type;

}

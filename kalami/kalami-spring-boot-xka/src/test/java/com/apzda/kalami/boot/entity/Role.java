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

import com.apzda.kalami.dictionary.Dict;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Created at 2023/7/7 15:15.
 *
 * @author ningGf
 * @version 1.0.0
 * @since 1.0.0
 **/
@Data
@TableName("t_roles")
public class Role {

    @TableId(type = IdType.ASSIGN_UUID)
    private String rid;

    private String name;

    @TableLogic
    private Integer del;

    @Dict(code = "test")
    private String type;

}

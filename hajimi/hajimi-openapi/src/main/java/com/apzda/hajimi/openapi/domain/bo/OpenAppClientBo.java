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
package com.apzda.hajimi.openapi.domain.bo;

import com.apzda.kalami.data.validation.Group;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开放平台应用客户端业务对象 open_app_client
 *
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class OpenAppClientBo {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { Group.Update.class })
    private Long id;

    /**
     * 客户端ID
     */
    @NotBlank(message = "客户端ID不能为空", groups = { Group.Update.class, Group.New.class })
    private String clientId;

    /**
     * 客户端名称
     */
    @NotBlank(message = "客户端名称不能为空", groups = { Group.Update.class, Group.New.class })
    private String clientName;

    /**
     * 客户端密钥
     */
    @NotBlank(message = "客户端密钥不能为空", groups = { Group.Update.class, Group.New.class })
    private String clientSecret;

    /**
     * 客户端私钥
     */
    @NotBlank(message = "客户端私钥不能为空", groups = { Group.Update.class, Group.New.class })
    private String clientPriKey;

    /**
     * 客户端公钥
     */
    @NotBlank(message = "客户端公钥不能为空", groups = { Group.Update.class, Group.New.class })
    private String clientPubKey;

    /**
     * 是否禁用
     */
    private Boolean disabled;

    /**
     * 备注
     */
    private String remark;

}

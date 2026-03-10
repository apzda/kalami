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

import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.dictionary.Dict;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Data
public class AgentDto {

    /**
     * 代理商ID
     */
    @NotNull(groups = Group.Update.class)
    @Null(groups = Group.New.class)
    private Long id;

    /**
     * 上级代理商ID
     */
    private Long parentId;

    /**
     * 代理商名称
     */
    @NotBlank(groups = Group.New.class)
    private String name;

    /**
     * 代理商简称
     */
    private String shortName;

    /**
     * 联系人姓名
     */
    private String contact;

    /**
     * 联系地址
     */
    private String address;

    /**
     * 代理商状态
     */
    @Dict
    private TenantStatus status;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 超管用户名
     */
    @NotBlank(groups = Group.New.class)
    private String username;

    /**
     * 超管姓名
     */
    private String nickname;

    /**
     * 超管密码
     */
    @NotBlank(groups = Group.New.class)
    private String password;

    /**
     * 确认超管密码
     */
    @NotBlank(groups = Group.New.class)
    private String confirmPassword;

    /**
     * 费率配置
     */
    private Map<String, Double> ratio;

    /**
     * 商户号
     */
    private Map<String, String> merNo;

    public String getShortName() {
        if (StringUtils.isBlank(shortName)) {
            return StringUtils.substring(name, 0, 5);
        }
        return shortName;
    }

}

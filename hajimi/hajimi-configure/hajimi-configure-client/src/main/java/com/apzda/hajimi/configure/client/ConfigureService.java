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
package com.apzda.hajimi.configure.client;

import com.apzda.hajimi.configure.Revision;
import com.apzda.hajimi.configure.client.dto.RestoreReq;
import com.apzda.hajimi.configure.client.dto.RevisionReq;
import com.apzda.hajimi.configure.client.dto.SaveReq;
import com.apzda.kalami.data.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
//@formatter:off
@FeignClient(
    name = "${kalami.cloud.feign.service.mashup.name:mashupSvc}",
    url = "${kalami.cloud.feign.service.mashup.url:}",
    contextId = "kalami.cloud.feign.service.ConfigureService",
    primary = false
)
//@formatter:on
public interface ConfigureService {

    /**
     * 保存配置
     */
    @PostMapping(value = "/_/mashupSvc/setting/save", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<Boolean> save(@RequestBody SaveReq request);

    /**
     * 获取配置版本列表
     */
    @PostMapping(value = "/_/mashupSvc/setting/revisions", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<List<Revision>> revisions(@RequestBody RevisionReq request);

    /**
     * 恢复配置到指定版本
     */
    @PostMapping(value = "/_/mashupSvc/setting/restore", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    Response<Boolean> restore(@RequestBody RestoreReq request);

    /**
     * 恢复配置到指定版本
     */
    @GetMapping(value = "/_/mashupSvc/setting/{settingCls}", produces = MediaType.APPLICATION_JSON_VALUE)
    Response<String> load(@PathVariable("settingCls") String settingCls);

}

/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.demo.user.api;

import com.apzda.kalami.demo.user.vo.UserVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author fengz (windywany@gmail.com)
 * @since 2025/05/14
 * @version 1.0.0
 */
@FeignClient(name = "${kalami.cloud.feign.service.user.name:userService}",
        url = "${kalami.cloud.feign.service.user.url:}", contextId = "com.apzda.kalami.user.api.UserApi",
        primary = false)
public interface UserApi {

    @GetMapping("/_/userSvc/user/{id}")
    UserVO getUser(@PathVariable("id") String id);

    @GetMapping(value = "/_/userSvc/user/authorities/{uid}", produces = { MediaType.APPLICATION_JSON_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE })
    List<SimpleGrantedAuthority> getAuthorities(@PathVariable String uid);

}

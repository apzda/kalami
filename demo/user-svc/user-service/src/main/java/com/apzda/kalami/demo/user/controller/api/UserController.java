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
package com.apzda.kalami.demo.user.controller.api;

import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.demo.store.feign.StoreDetailService;
import com.apzda.kalami.demo.store.feign.StoreService;
import com.apzda.kalami.demo.user.vo.UserVO;
import com.apzda.kalami.security.annotation.Authenticated;
import com.apzda.kalami.user.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author fengz (windywany@gmail.com)
 * @since 2025/05/15
 * @version 1.0.0
 */

@Slf4j
@RestController("userSvcUserController")
@RequestMapping("/userSvc/user/")
@RequiredArgsConstructor
public class UserController {

    private final StoreService storeService;

    private final StoreDetailService storeDetailService;

    /**
     * 对name说好
     * @param name 用户名
     * @return 招呼说明
     */
    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name) {
        return "Hello " + name;
    }

    /**
     * 打招呼2
     */
    @GetMapping("")
    @PreAuthorize("@authz.iCan('view:user')")
    public String greeting(@RequestParam("name") String name, CurrentUser user) {
        return "Hello " + name + ", id=" + user.getUid() + ", s=" + storeService.count(name) + ", d="
                + storeDetailService.countDetail(name);
    }

    /**
     * 分页接口
     */
    @GetMapping("/names")
    public Response<Paged<UserVO>> names(Pageable pager) {
        Paged<UserVO> page = new Paged<>(
                new PageImpl<>(List.of(new UserVO("1", "a"), new UserVO("2", "b"), new UserVO("3", "c")), pager, 3));
        log.info("分页: {}", pager);
        return Response.success(page).alert("Hello World");
    }

    @PostMapping("/heiehi")
    @Authenticated
    public Response<String> heihei() {

        return Response.success("Heihei");
    }

}

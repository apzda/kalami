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

package com.apzda.kalami.security.web.resolver;

import com.apzda.kalami.security.web.dto.CardDto;
import com.apzda.kalami.security.web.dto.StaffDto;
import com.apzda.kalami.user.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/19
 * @version 1.0.0
 */
@RestController
public class TestController {

    @GetMapping("/test/ok")
    public String ok(CurrentUser currentUser) {
        return currentUser.getUid();
    }

    @GetMapping("/test/authority")
    @PreAuthorize("@authz.iCan('view:gsvc.user')")
    public String hasAuthority(CurrentUser currentUser) {
        return currentUser.getUid();
    }

    @GetMapping("/test/ican/{id}")
    @PreAuthorize("@authz.iCan('view:gsvc.user',#id)")
    public String hasAuthority(CurrentUser currentUser, @PathVariable("id") String id) {
        return id;
    }

    @PostMapping("/test/card")
    @PreAuthorize("@authz.isMine(#card)")
    public String isMine(@RequestBody CardDto card) {
        return card.getCreatedBy();
    }

    @GetMapping("/test/card/{id}")
    @PreAuthorize("@authz.isMine(#id)")
    public String isMine(@PathVariable String id) {
        return id;
    }

    @PostMapping("/test/staff")
    @PreAuthorize("authenticated && @authz.isTenanted(#staffDto)")
    public String isOwned(@RequestBody StaffDto staffDto) {
        return staffDto.getTenantId().toString();
    }

    @GetMapping("/test/staff/{id}")
    @PreAuthorize("authenticated && @authz.isTenanted(#id)")
    public String isOwned(@PathVariable String id) {
        return id;
    }

}

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

package com.apzda.kalami.security.authorization;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/19
 * @version 1.0.0
 */
@JsonTest
@ContextConfiguration(classes = AsteriskPermissionEvaluatorTest.InternalConfiguration.class)
@ActiveProfiles("test")
class AsteriskPermissionEvaluatorTest {

    @Autowired
    private AsteriskPermissionEvaluator evaluator;

    @Test
    @WithMockUser(username = "gsvc", authorities = "view,edit:gsvc.user.*")
    void hasPermission_should_be_ok() {
        // given
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val permit = evaluator.hasPermission(authentication, "1", "view:gsvc.user");
        assertThat(permit).isTrue();
        val permit2 = evaluator.hasPermission(authentication, "2", "edit:gsvc.user");
        assertThat(permit2).isTrue();
        val permit3 = evaluator.hasPermission(authentication, "3", "add:gsvc.user");
        assertThat(permit3).isFalse();
    }

    @Test
    @WithMockUser(username = "gsvc", authorities = "*:gsvc.user.*")
    void hasPermission_asterisk_should_be_ok() {
        // given
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val permit = evaluator.hasPermission(authentication, "1", "view:gsvc.user");
        assertThat(permit).isTrue();
        val permit2 = evaluator.hasPermission(authentication, "2", "edit:gsvc.user");
        assertThat(permit2).isTrue();
        val permit3 = evaluator.hasPermission(authentication, "3", "add:gsvc.user");
        assertThat(permit3).isTrue();
    }

    @Test
    @WithMockUser(username = "gsvc", authorities = "view:gsvc.*.*")
    void hasPermission_view_should_be_ok() {
        // given
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val permit = evaluator.hasPermission(authentication, null, "view:gsvc.user");
        assertThat(permit).isTrue();
        val permit2 = evaluator.hasPermission(authentication, "3", "new:gsvc.user");
        assertThat(permit2).isFalse();
        val permit3 = evaluator.hasPermission(authentication, "3", "view:gsvc.user");
        assertThat(permit3).isTrue();
    }

    @Test
    @WithMockUser(username = "gsvc", authorities = "view,new:gsvc.*.1,2,3,4")
    void hasPermission_multiple_segment_should_be_ok() {
        // given
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val permit = evaluator.hasPermission(authentication, null, "view:gsvc.user");
        assertThat(permit).isFalse();
        val permit2 = evaluator.hasPermission(authentication, "3", "new:gsvc.admin");
        assertThat(permit2).isTrue();
        val permit3 = evaluator.hasPermission(authentication, "4", "view:gsvc.admin");
        assertThat(permit3).isTrue();
    }

    @Configuration
    static class InternalConfiguration {
        @Bean
        PermissionChecker checker(){
            return new PermissionChecker() {
                @Override
                public Boolean check(Authentication authentication, Object obj, String permission) {
                    return null;
                }

                @Override
                public Boolean check(Authentication authentication, Serializable targetId, String targetType, String permission) {
                    return null;
                }

                @Override
                public boolean supports(Class<?> objClazz) {
                    return false;
                }

                @Override
                public boolean supports(String targetType) {
                    return false;
                }
            };
        }
        @Bean
        AsteriskPermissionEvaluator asteriskPermissionEvaluator(ObjectProvider<PermissionChecker> permissionChecker) {
            return new AsteriskPermissionEvaluator(permissionChecker);
        }

    }

}

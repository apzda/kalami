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

import com.apzda.kalami.security.authorization.AsteriskPermissionEvaluator;
import com.apzda.kalami.security.authorization.AuthorizationLogicCustomizer;
import com.apzda.kalami.security.authorization.PermissionChecker;
import com.apzda.kalami.security.config.SecurityConfigProperties;
import com.apzda.kalami.security.context.SpringSecurityUserProvider;
import com.apzda.kalami.security.web.dto.CardDto;
import com.apzda.kalami.security.web.dto.StaffDto;
import com.apzda.kalami.user.CurrentUserProvider;
import com.apzda.kalami.user.TenantManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/19
 * @version 1.0.0
 */
@WebMvcTest(controllers = TestController.class)
@ContextConfiguration(classes = CurrentUserParamResolverTest.WebMvcConfigure.class)
@Import({ CurrentUserParamResolverTest.WebMvcConfigure.class, TestController.class })
@ActiveProfiles("test")
class CurrentUserParamResolverTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SecurityConfigProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser("gsvc")
    void ok() throws Exception {
        mvc.perform(get("/test/ok")).andExpect(status().isOk()).andExpect(content().string("gsvc"));
    }

    @Test
    void ok1() throws Exception {
        mvc.perform(get("/test/ok").accept(MediaType.TEXT_HTML_VALUE))
            .andExpect(status().is(302))
            .andExpect(header().exists("Location"));
    }

    @Test
    @WithMockUser("gsvc")
    void isMine() throws Exception {
        val card = new CardDto();
        card.setCreatedBy("gsvc");
        mvc.perform(post("/test/card").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(card))
            .accept(MediaType.TEXT_HTML)).andExpect(status().isOk()).andExpect(content().string("gsvc"));
    }

    @Test
    @WithMockUser("gsvc")
    void isMine_withStringId() throws Exception {
        mvc.perform(get("/test/card/gsvc").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string("gsvc"));
    }

    @Test
    @WithMockUser("gsvc")
    void isOwned() throws Exception {
        val staffDto = new StaffDto();
        staffDto.setTenantId(1L);
        mvc.perform(post("/test/staff").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(staffDto))
            .accept(MediaType.TEXT_HTML)).andExpect(status().isOk()).andExpect(content().string("1"));

        staffDto.setTenantId(2L);
        mvc.perform(post("/test/staff").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(staffDto))
            .accept(MediaType.TEXT_HTML)).andExpect(status().isOk()).andExpect(content().string("2"));

        staffDto.setTenantId(3L);
        mvc.perform(post("/test/staff").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(staffDto))
            .accept(MediaType.TEXT_HTML)).andExpect(status().isOk()).andExpect(content().string("3"));
    }

    @Test
    @WithMockUser("gsvc")
    void isOwned_withStringId() throws Exception {
        mvc.perform(get("/test/staff/1").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string("1"));
        mvc.perform(get("/test/staff/2").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string("2"));
        mvc.perform(get("/test/staff/3").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string("3"));
    }

    @Test
    @WithMockUser("gsvc")
    void isNotMine() throws Exception {
        val card = new CardDto();
        card.setCreatedBy("gsvcx");
        mvc.perform(post("/test/card").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(card))
            .accept(MediaType.TEXT_HTML)).andExpect(status().is(403));
    }

    @Test
    @WithMockUser("gsvc")
    void isNotMine_withStringId() throws Exception {
        mvc.perform(get("/test/card/gsvcx").accept(MediaType.TEXT_HTML)).andExpect(status().is(403));
    }

    @Test
    @WithMockUser("gsvc")
    void isNotOwned() throws Exception {
        val staffDto = new StaffDto();
        staffDto.setTenantId(5L);
        mvc.perform(post("/test/staff").with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(staffDto))
            .accept(MediaType.TEXT_HTML)).andExpect(status().is(403));
    }

    @Test
    @WithMockUser("gsvc")
    void isNotOwned_withStringId() throws Exception {
        mvc.perform(get("/test/staff/4").accept(MediaType.TEXT_HTML)).andExpect(status().is(403));
    }

    @Test
    @WithMockUser(value = "gsvc", authorities = "view,new:gsvc.user.*")
    void iCan_should_ok() throws Exception {
        mvc.perform(get("/test/authority").accept(MediaType.TEXT_HTML)).andExpect(status().isOk());
        mvc.perform(get("/test/ican/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(value = "gsvc", authorities = "view:gsvc.user.1,2,3")
    void iCan_with_id_should_ok() throws Exception {
        mvc.perform(get("/test/authority").accept(MediaType.TEXT_HTML)).andExpect(status().is(403));
        mvc.perform(get("/test/ican/3").accept(MediaType.TEXT_HTML)).andExpect(status().isOk());
        mvc.perform(get("/test/ican/4").accept(MediaType.TEXT_HTML)).andExpect(status().is(403));
    }

    @Configuration
    @EnableConfigurationProperties(SecurityConfigProperties.class)
    @EnableMethodSecurity
    static class WebMvcConfigure implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new CurrentUserParamResolver());
        }

        @Bean
        PermissionEvaluator permissionEvaluator(ObjectProvider<PermissionChecker> permissionEvaluatorProvider) {
            return new AsteriskPermissionEvaluator(permissionEvaluatorProvider);
        }

        @Bean
        AuthorizationLogicCustomizer authz(PermissionEvaluator permissionEvaluator) {
            return new AuthorizationLogicCustomizer(permissionEvaluator);
        }

        @Bean
        TenantManager<String> tenantManager() {
            return new TenantManager<>() {
                @Override
                @NonNull
                protected String[] getTenantIds() {
                    return new String[] { "1", "2", "3" };
                }
            };
        }

        @Bean
        CurrentUserProvider currentUserProvider() {
            return new SpringSecurityUserProvider();
        }

    }

}

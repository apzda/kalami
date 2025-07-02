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
package com.apzda.kalami.web.context;

import com.apzda.kalami.context.KalamiContextHolder;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
class KalamiContextHolderTest {

    @BeforeEach
    void before() {
        KalamiContextHolder.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void x_forwarded_should_work() {
        // given
        val request = new MockHttpServletRequest("GET", "https://kalami.apzda.com");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("x-forwarded-for", "192.168.1.1");
        request.addHeader("x-forwarded-proto", "ws");
        request.addHeader("x-forwarded-host", "kalami.apzda.com");
        val servletRequestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        // then
        assertThat(KalamiContextHolder.getSchema()).isEqualTo("ws");
        assertThat(KalamiContextHolder.getRemoteIp()).isEqualTo("192.168.1.1");
        assertThat(KalamiContextHolder.getRemoteAddr()).isEqualTo("127.0.0.1");
        assertThat(KalamiContextHolder.getHostName()).isEqualTo("kalami.apzda.com");
    }

    @Test
    void direct_should_work() {
        // given
        val request = new MockHttpServletRequest("GET", "https://kalami.apzda.com");
        request.setRemoteAddr("127.0.0.1");
        request.setScheme("https");
        request.addHeader("host", "kalami.apzda.com");
        val servletRequestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);

        // then
        assertThat(KalamiContextHolder.getSchema()).isEqualTo("https");
        assertThat(KalamiContextHolder.getRemoteIp()).isEqualTo("127.0.0.1");
        assertThat(KalamiContextHolder.getRemoteAddr()).isEqualTo("127.0.0.1");
        assertThat(KalamiContextHolder.getHostName()).isEqualTo("kalami.apzda.com");
    }

}

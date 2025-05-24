package com.apzda.kalami.web.context;

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

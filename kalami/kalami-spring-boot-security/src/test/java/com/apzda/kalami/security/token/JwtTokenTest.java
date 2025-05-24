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

package com.apzda.kalami.security.token;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/20
 * @version 1.0.0
 */
class JwtTokenTest {

    @Test
    void parseFlag() {
        // given
        JwtToken token = DefaultToken.builder().uid("123").build();
        token.setMfa(2);
        token.setCredentialsExpired(true);
        token.setActivated(true);
        token.setDisabled(true);
        val flag = token.getFlag();

        val token1 = DefaultToken.builder().build();
        JwtToken.parseFlag(Integer.parseInt(flag), token1);

        assertThat(token1.getMfa()).isEqualTo(2);
        assertThat(token1.getCredentialsExpired()).isTrue();
        assertThat(token1.getActivated()).isTrue();
        assertThat(token1.getBound()).isFalse();
        assertThat(token1.getLocked()).isFalse();
        assertThat(token1.getDisabled()).isTrue();
    }

}

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
package com.apzda.kalami.sanitize;

import com.apzda.kalami.sanitizer.RegexpSanitizer;
import com.apzda.kalami.utils.SanitizeUtils;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
class SanitizeUtilsTest {

    @Test
    void getSanitizers() {
        val sanitizer = SanitizeUtils.getSanitizer(RegexpSanitizer.class);
        assertThat(sanitizer).isNotNull();
    }

    @Test
    void sanitize() {
        // given
        TestVo testVo = new TestVo();
        testVo.setPhone("13166666666");
        testVo.setPhone1("13166666666");
        // when
        val sanitized = SanitizeUtils.sanitize(testVo);
        // then
        assertThat(sanitized).isNotNull();
        assertThat(sanitized.getPhone()).isEqualTo("131****6666");
        assertThat(sanitized.getPhone1()).isEqualTo("131****6666");
    }

}

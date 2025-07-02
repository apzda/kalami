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
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
class RegexpSanitizerTest {

    @Test
    void sanitize() {
        // given
        String phone = "13166666666";
        String phone1 = "+86 13166666666";
        String phone2 = "+886 13166666666";
        String[] configure = new String[] { "^((\\+\\d{2,4}\\s+)?\\d{3})\\d{4}(\\d{4})$", "$1****$3" };

        val sanitizer = new RegexpSanitizer();
        // when
        val sanitized = sanitizer.sanitize(phone, configure);
        val sanitized1 = sanitizer.sanitize(phone1, configure);
        val sanitized2 = sanitizer.sanitize(phone2, configure);
        // then
        assertThat(sanitized).isEqualTo("131****6666");
        assertThat(sanitized1).isEqualTo("+86 131****6666");
        assertThat(sanitized2).isEqualTo("+886 131****6666");
    }

}

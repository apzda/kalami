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
package com.apzda.kalami.boot.vo;

import com.apzda.kalami.boot.enums.TestStatus;
import com.apzda.kalami.boot.enums.TestStatus2;
import com.apzda.kalami.boot.enums.TestStatus3;
import com.apzda.kalami.boot.sanitize.PhoneNumberSanitizer;
import com.apzda.kalami.boot.transformer.TestTransformer;
import com.apzda.kalami.dictionary.Dict;
import com.apzda.kalami.sanitizer.Sanitized;
import lombok.Data;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Data
public class TestVo {

    private String name;

    @Dict
    private TestStatus status;

    @Dict("description")
    private TestStatus2 status2;

    @Dict
    private TestStatus3 status3;

    @Sanitized(sanitizer = PhoneNumberSanitizer.class)
    private String phone;

    private String phone1;

    @Dict(transformer = TestTransformer.class)
    private String phone2;

    @Sanitized(sanitizer = PhoneNumberSanitizer.class)
    public String getPhone1() {
        return phone1;
    }

}

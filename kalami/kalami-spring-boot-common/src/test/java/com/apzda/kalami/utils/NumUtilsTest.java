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
package com.apzda.kalami.utils;

import lombok.val;
import org.junit.jupiter.api.Test;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
class NumUtilsTest {

    @Test
    void growthRatio() {
        val ratio1 = NumUtils.growthRatio(49, 27, 1);

        System.out.println("ratio1 = " + ratio1.toString());

        val ratio2 = NumUtils.growthRatio(49, 27, 2);

        System.out.println("ratio2 = " + ratio2.toString());

        val ratio3 = NumUtils.growthRatio(49, 27, 3);
        System.out.println("ratio3 = " + ratio3.toString());

        val ratio4 = NumUtils.growthRatio(49, 27, 4);
        System.out.println("ratio4 = " + ratio4.toString());
    }

}

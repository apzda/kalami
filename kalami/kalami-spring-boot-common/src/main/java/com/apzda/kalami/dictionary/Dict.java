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
package com.apzda.kalami.dictionary;

import java.lang.annotation.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Dict {

    /**
     * 1. {@link #table()}不为空时，对应表中代表label的字段,空或是"*"时会返回整行数据 <br/>
     * 2. {@link #code()}不为空,{@link #table()}为空时，代表系统字典表里的标签字段<br/>
     * 3. {@linkplain #transformer()}不为空时，"*"表示应该返回整个对象.
     */
    String value() default "";

    /**
     * 1. {@link #table()}不为空时，对应表中关联的字段 <br/>
     * 2. {@link #table()}为空时，对应系统字典表里的标签字段
     */
    String code() default "";

    String table() default "";

    Class<?> entity() default Void.class;

    Class<? extends Transformer> transformer() default Transformer.class;

}

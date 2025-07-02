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
package com.apzda.kalami.jackson;

import com.apzda.kalami.utils.ComTool;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Getter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * @author john <luxi520cn@163.com>
 */
@Getter
public class JacksonCustomizer implements Jackson2ObjectMapperBuilderCustomizer {

    private final Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder;

    private final ObjectMapper objectMapper;

    public JacksonCustomizer() {
        this.jacksonObjectMapperBuilder = new Jackson2ObjectMapperBuilder();
        customize(this.jacksonObjectMapperBuilder);
        this.objectMapper = this.jacksonObjectMapperBuilder.build()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        // 全局配置序列化返回 JSON 处理
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 超出JS最大最小值，序列化为字符串
        javaTimeModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
        javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
        javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
        javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        // LocalDateTime和LocalDate处理
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(ComTool.getDateTimeFormatter(ComTool.YYYY_MM_DD_HH_MM_SS)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(ComTool.getDateTimeFormatter(ComTool.YYYY_MM_DD_HH_MM_SS)));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(ComTool.getDateTimeFormatter(ComTool.YYYY_MM_DD)));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(ComTool.getDateTimeFormatter(ComTool.YYYY_MM_DD)));
        jacksonObjectMapperBuilder.modules(javaTimeModule, new Jdk8Module());
        jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
    }

}

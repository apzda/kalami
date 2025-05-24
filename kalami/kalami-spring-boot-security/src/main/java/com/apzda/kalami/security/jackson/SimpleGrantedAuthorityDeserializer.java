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

package com.apzda.kalami.security.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/22
 * @version 1.0.0
 */
public class SimpleGrantedAuthorityDeserializer extends StdDeserializer<SimpleGrantedAuthority> {

    public SimpleGrantedAuthorityDeserializer() {
        this(null);
    }

    public SimpleGrantedAuthorityDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public SimpleGrantedAuthority deserialize(JsonParser jp, DeserializationContext ctx)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String authority = node.get("authority").asText();

        return new SimpleGrantedAuthority(authority);
    }

}

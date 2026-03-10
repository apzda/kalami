/*
 * Copyright 2026 the original author or authors.
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
package com.apzda.hajimi.dy.dto;

import com.apzda.kalami.context.KalamiContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.util.Lazy;

import java.util.HashMap;
import java.util.Optional;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public class CommonCallbackDto extends HashMap<String, Object> {

    private final static Lazy<ObjectMapper> mapper = Lazy.of(() -> KalamiContextHolder.getBean(ObjectMapper.class));

    public String event() {
        return (String) getOrDefault("event", "");
    }

    public String clientKey() {
        return (String) getOrDefault("client_key", "");
    }

    public String fromUserId() {
        return (String) getOrDefault("from_user_id", null);
    }

    public String toUserId() {
        return (String) getOrDefault("to_user_id", null);
    }

    public <T> Optional<T> content(Class<T> clazz) {
        if (containsKey("content")) {
            val content = this.get("content");
            try {
                return Optional.of(mapper.get().convertValue(content, clazz));
            }
            catch (Exception e) {
                log.error("类型转换出错", e);
            }
        }

        return Optional.empty();
    }

}

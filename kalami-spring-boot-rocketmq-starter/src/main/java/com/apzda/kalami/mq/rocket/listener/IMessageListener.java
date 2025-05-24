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

package com.apzda.kalami.mq.rocket.listener;

import com.apzda.kalami.mq.rocket.message.IMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.apis.message.MessageView;

import java.nio.charset.StandardCharsets;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/22
 * @version 1.0.0
 */
public interface IMessageListener<T extends IMessage<T, E>, E extends Enum<?>> {

    boolean onMessage(@Nonnull T message, E tag, @Nonnull MessageView messageView);

    @SuppressWarnings("unchecked")
    default boolean onMessage(@Nonnull MessageView messageView, @Nonnull TypeReference<?> typeReference,
            @Nonnull Class<?> tagClz, @Nonnull ObjectMapper objectMapper) throws JsonProcessingException {
        val content = StandardCharsets.UTF_8.decode(messageView.getBody()).toString();
        val message = decode(content, typeReference, objectMapper);
        val s = messageView.getTag().orElse(null);
        if (StringUtils.isBlank(s)) {
            return onMessage(message, null, messageView);
        }

        val e = (E) objectMapper.readValue(String.format("\"%s\"", s), tagClz);
        return onMessage(message, e, messageView);
    }

    @SuppressWarnings("unchecked")
    default T decode(@Nonnull String content, @Nonnull TypeReference<?> typeReference,
            @Nonnull ObjectMapper objectMapper) throws JsonProcessingException {
        return (T) objectMapper.readValue(content, typeReference);
    }

}

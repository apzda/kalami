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

package com.apzda.kalami.mq.rocket.test.listener;

import com.apzda.kalami.mq.rocket.listener.IMessageListener;
import com.apzda.kalami.mq.rocket.test.Tags;
import com.apzda.kalami.mq.rocket.test.message.SimpleMessage;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.message.MessageView;

/**
 * @author fengz (windywany@gmail.com)
 * @version 3.4.0
 * @since 3.4.0
 **/
@Slf4j
public class DemoMessageListener implements IMessageListener<SimpleMessage, Tags> {

    @Override
    public boolean onMessage(@Nonnull SimpleMessage message, Tags tag, @Nonnull MessageView messageView) {
        log.info("Receive message from topic: {}, tag: {}, message: {}", messageView.getTopic(), tag,
                message.getContent());
        return true;
    }

}

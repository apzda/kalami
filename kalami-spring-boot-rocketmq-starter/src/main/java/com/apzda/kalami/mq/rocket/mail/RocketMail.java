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

package com.apzda.kalami.mq.rocket.mail;

import com.apzda.kalami.mq.mail.AbstractMail;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/22
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RocketMail extends AbstractMail {

    private String topic;

    private String tags;

    private String group;

    @Override
    public void setRecipients(String recipients) {
        super.setRecipients(recipients);
        val segments = StringUtils.split(recipients, ":");
        if (segments != null) {
            if (segments.length > 1) {
                this.topic = segments[0];
                this.tags = segments[1];
                if (segments.length > 2) {
                    this.group = segments[2];
                }
            }
            else if (segments.length == 1) {
                this.topic = segments[0];
            }
        }
    }

}

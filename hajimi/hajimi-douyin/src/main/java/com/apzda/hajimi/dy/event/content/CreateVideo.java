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
package com.apzda.hajimi.dy.event.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@NoArgsConstructor
@Data
public class CreateVideo {

    @JsonProperty("share_id")
    private String shareId;

    @JsonProperty("item_id")
    private String itemId;

    @JsonProperty("video_id")
    private String videoId;

    @JsonProperty("has_default_hashtag")
    private Boolean hasDefaultHashtag;

}

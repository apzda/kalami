/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/17
 * @version 1.0.0
 */
public abstract class MediaTypeUtil {

    public static final MediaType TEXT_MASK = MediaType.parseMediaType("text/*");

    public static final MediaType IMAGE = MediaType.parseMediaType("image/*");

    public static boolean isImage(@Nullable List<MediaType> mediaTypes) {
        return compatibleMediaType(IMAGE, mediaTypes) != null;
    }

    public static boolean isText(@Nullable List<MediaType> mediaTypes) {
        return compatibleMediaType(TEXT_MASK, mediaTypes) != null;
    }

    public static MediaType compatibleMediaType(MediaType mediaType, List<MediaType> mediaTypes) {
        if (mediaType != null && !CollectionUtils.isEmpty(mediaTypes)) {
            val contentType = mediaTypes.get(0);
            if (contentType.isWildcardType()) {
                return null;
            }

            if (contentType.isCompatibleWith(mediaType)) {
                return contentType;
            }
        }

        return null;
    }

    @Nullable
    public static String getUrl(String url, List<MediaType> contentTypes) {
        if (StringUtils.isNotBlank(url) && !CollectionUtils.isEmpty(contentTypes)) {
            val compatibleWith = compatibleMediaType(TEXT_MASK, contentTypes);
            if (compatibleWith != null) {
                return url;
            }
        }

        return null;
    }

}

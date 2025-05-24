/*
 * This file is part of gsvc created at 2023/9/13 by ningGf.
 */
package com.apzda.kalami.data;

import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

/**
 * @author fengz (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
public enum MessageType {

    TOAST, ALERT, NOTIFY, NONE;

    @Nullable
    public static MessageType fromString(String text) {
        if ("toast".equalsIgnoreCase(text)) {
            return TOAST;
        }
        else if ("alert".equalsIgnoreCase(text)) {
            return ALERT;
        }
        else if ("notify".equalsIgnoreCase(text)) {
            return NOTIFY;
        }
        else if ("none".equalsIgnoreCase(text)) {
            return NONE;
        }
        else if (StringUtils.isNoneBlank(text)) {
            return NOTIFY;
        }

        return null;
    }

}

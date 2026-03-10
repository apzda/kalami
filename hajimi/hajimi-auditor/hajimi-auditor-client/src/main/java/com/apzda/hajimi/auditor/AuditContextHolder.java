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
package com.apzda.hajimi.auditor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public abstract class AuditContextHolder {

    private static final ThreadLocal<Context> context = new InheritableThreadLocal<>();

    static void create() {
        context.set(new Context(null, null, new HashMap<>()));
    }

    public static void setContext(Context context) {
        AuditContextHolder.context.set(context);
    }

    public static Context getContext() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }

    public static void restore(Context oldContext) {
        context.remove();
        if (oldContext != null) {
            context.set(oldContext);
        }
    }

    @Getter
    public static class Context {

        @Setter
        private Object newValue;

        @Setter
        private Object oldValue;

        @Setter
        private String userId;

        @Setter
        private String tenantId;

        private final Map<String, Object> data;

        Context(Object newValue, Object oldValue, Map<String, Object> data) {
            this.newValue = newValue;
            this.oldValue = oldValue;
            this.data = data;
        }

        public void set(String name, Object value) {
            this.data.put(name, value);
        }

    }

}

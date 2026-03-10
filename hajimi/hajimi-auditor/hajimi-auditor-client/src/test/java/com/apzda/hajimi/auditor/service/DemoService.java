/*
 * Copyright 2023-2026 the original author or authors.
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
package com.apzda.hajimi.auditor.service;

import com.apzda.hajimi.auditor.Audit;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.domain.AuditLog;
import com.apzda.kalami.event.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Service
@RequiredArgsConstructor
public class DemoService {

    private final ApplicationEventPublisher eventPublisher;

    @Audit(activity = "test", succTpl = "error message is {}", args = "#returnObj.errMsg")
    public Response<Boolean> hello() {
        return Response.error(1, "error message");
    }

    @Audit(activity = "test", succTpl = "error message is {}, arg is {}", args = { "#returnObj?.errMsg", "#msg" })
    public Response<Boolean> hello(String msg) {
        return Response.error(1, "error " + msg);
    }

    @Audit(activity = "test", succTpl = "error message is {}, arg is {}", args = { "#returnObj?.errMsg", "#msg" },
            error = "#{ 'Arg \"' + #msg + '\":' + #throwExp.message }")
    public Response<Boolean> hello2(String msg) {
        throw new RuntimeException(msg + " is invalid");
    }

    @Audit(activity = "test", errorTpl = "exception message is {}, arg is {}", args = { "#throwExp?.message", "#msg" },
            async = false)
    public Response<Boolean> hello3(String msg) {
        throw new RuntimeException(msg + " is invalid");
    }

    public void publishEvent() {
        val audit = new AuditLog();
        audit.setActivity("test");
        audit.setMessage("TEST");
        audit.getArgs().add("112");
        val auditEvent = new AuditEvent(audit);
        eventPublisher.publishEvent(auditEvent);
    }

}

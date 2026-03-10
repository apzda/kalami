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
package com.apzda.hajimi.auditor;

import com.apzda.hajimi.auditor.logging.AuditLoggerFactory;
import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.data.domain.AuditLog;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUserProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class AuditLogAdvisor {

    private final static org.slf4j.Logger log = LoggerFactory.getLogger("audit");

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    private final ParserContext parserContext = new TemplateParserContext();

    private final ObjectMapper objectMapper;

    private final AuditLoggerFactory auditLoggerFactory;

    @Around("@annotation(com.apzda.hajimi.auditor.Audit)")
    public Object interceptor(@Nonnull ProceedingJoinPoint pjp) throws Throwable {
        val args = pjp.getArgs();
        val signature = (MethodSignature) pjp.getSignature();
        val method = signature.getMethod();
        val ann = method.getAnnotation(Audit.class);
        val activity = StringUtils.defaultIfBlank(ann.value(), ann.activity());
        val target = ann.target();
        val oldContext = AuditContextHolder.getContext();
        try {
            if (StringUtils.isBlank(activity)) {
                log.warn("No activity specified for method {}", method);
            }
            AuditContextHolder.create();
            Object returnObj = null;
            Exception lastEx = null;
            try {
                returnObj = pjp.proceed(args);
            }
            catch (Exception e) {
                lastEx = e;
            }
            val ctx = AuditContextHolder.getContext();
            val currentUser = CurrentUserProvider.getCurrentUser();
            val userId = StringUtils.defaultIfBlank(ctx.getUserId(),
                    StringUtils.defaultIfBlank(currentUser.getUid(), "0"));
            val tenantId = StringUtils.defaultIfBlank(ctx.getTenantId(), TenantManager.tenantId("0"));
            val ip = KalamiContextHolder.getRemoteIp();
            val device = currentUser.getDevice();
            val builder = new AuditLog();
            builder.setTimestamp(System.currentTimeMillis());
            builder.setUserId(userId);
            builder.setActivity(activity);
            builder.setTarget(target);
            builder.setTenantId(tenantId);
            builder.setIp(ip);
            if (device != null) {
                builder.setDevice(device);
            }
            if (lastEx == null) {
                builder.setLevel(StringUtils.defaultIfBlank(ann.level(), "info"));
            }
            else {
                builder.setLevel("error");
            }
            val runAs = currentUser.getRunAs();
            if (runAs != null) {
                builder.setRunas(runAs);
            }
            val context = new StandardEvaluationContext(this);
            context.setVariable("returnObj", returnObj);
            context.setVariable("throwExp", transform(lastEx));
            context.setVariable("isThrow", lastEx != null);
            context.setVariable("newValue", ctx.getNewValue());
            context.setVariable("oldValue", ctx.getOldValue());
            context.setVariable("cData", ctx.getData());
            if (ctx.getNewValue() != null) {
                builder.setNewValue(ctx.getNewValue());
            }
            if (ctx.getOldValue() != null) {
                builder.setOldValue(ctx.getOldValue());
            }
            val parameters = method.getParameters();
            var i = 0;
            for (Parameter parameter : parameters) {
                val name = parameter.getName();
                context.setVariable(name, args[i++]);
            }

            val throwObj = lastEx;
            audit(ann, context, builder, throwObj);
            if (lastEx != null) {
                throw lastEx;
            }
            return returnObj;
        }
        finally {
            AuditContextHolder.restore(oldContext);
        }
    }

    public boolean isTrue(Object obj) {
        if (obj == null) {
            return false;
        }
        else if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        else if (obj instanceof String) {
            return StringUtils.isNotBlank((String) obj);
        }
        else if (obj instanceof Collection) {
            return !((Collection<?>) obj).isEmpty();
        }
        else if (obj instanceof Map) {
            return !((Map<?, ?>) obj).isEmpty();
        }
        return true;
    }

    public boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        else if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        return false;
    }

    public String any(String... strings) {
        return StringUtils.firstNonBlank(strings);
    }

    private void audit(Audit ann, StandardEvaluationContext context, AuditLog auditLog, Exception lastEx) {
        try {
            var template = ann.succTpl();
            var message = ann.message();

            if (lastEx != null) {
                val error = ann.error();
                if (StringUtils.isBlank(error)) {
                    val errTpl = ann.errorTpl();
                    if (StringUtils.isBlank(errTpl)) {
                        message = lastEx.getMessage();
                    }
                    else {
                        template = errTpl;
                    }
                }
                else {
                    message = error;
                }
            }

            if (StringUtils.isNotBlank(message)) {
                val evaluate = message.startsWith("#{") && message.endsWith("}");
                if (evaluate) {
                    try {
                        val expression = expressionParser.parseExpression(message, parserContext);
                        val msg = expression.getValue(context, String.class);
                        auditLog.setMessage(msg);
                    }
                    catch (Exception e) {
                        auditLog.setLevel("warn");
                        auditLog.setMessage(e.getMessage());
                    }
                }
                else {
                    auditLog.setMessage(message);
                }
            }
            else if (StringUtils.isNotBlank(template)) {
                auditLog.setTemplate(true);
                auditLog.setMessage(template);
                val arg = ann.args();
                var idx = 0;
                var argVal = "";
                for (String value : arg) {
                    if (value.startsWith("#")) {
                        try {
                            val expression = expressionParser.parseExpression(value);
                            argVal = expression.getValue(context, String.class);
                        }
                        catch (Exception e) {
                            log.warn("Cannot parse arg: {}", value);
                            argVal = value;
                        }
                    }
                    else {
                        argVal = value;
                    }
                    argVal = StringUtils.defaultIfBlank(argVal, "");
                    auditLog.getArgs().add(argVal);
                }
            }

            val target = auditLog.getTarget();
            if (Strings.CS.startsWith(target, "#{") && Strings.CS.endsWith(target, "}")) {
                try {
                    val expression = expressionParser.parseExpression(target, parserContext);
                    auditLog.setTarget(expression.getValue(context, String.class));
                }
                catch (Exception e) {
                    log.warn("Cannot parse target: {}", target);
                }
            }

            val logger = auditLoggerFactory.create(auditLog.getActivity());
            logger.log(auditLog);
        }
        catch (Exception e) {
            try {
                log.warn("Cannot send audit log: {} - {}", objectMapper.writeValueAsString(auditLog), e.getMessage(),
                        e);
            }
            catch (Exception ignored) {
            }
        }
    }

    private Throwable transform(Throwable e) {
        if (e == null) {
            return e;
        }
        do {
            val message = e.getMessage();
            if (StringUtils.isNotBlank(message)) {
                return e;
            }
            if (e.getCause() == null) {
                return e;
            }
            e = e.getCause();
        }
        while (true);
    }

}

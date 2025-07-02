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
package com.apzda.kalami.security.aop;

import com.apzda.kalami.security.annotation.Subscribed;
import com.apzda.kalami.tenant.TenantManager;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // 在AuditLogAdvisor之后
@RequiredArgsConstructor
public class SubscribedAuthorityAspect {

    @Around("@annotation(com.apzda.kalami.security.annotation.Subscribed)")
    public Object proceed(@Nonnull ProceedingJoinPoint pjp) throws Throwable {
        val signature = (MethodSignature) pjp.getSignature();
        val method = signature.getMethod();
        val ann = method.getAnnotation(Subscribed.class);
        val value = ann.value();
        val subscriptions = TenantManager.subscriptions();

        if (CollectionUtils.isEmpty(subscriptions)) {
            throw new AccessDeniedException("未订阅任何服务");
        }
        if (StringUtils.isBlank(value)) {
            return pjp.proceed();
        }
        val subscription = subscriptions.get(value);
        if (subscription == null) {
            throw new AccessDeniedException(String.format("【%s】服务未订阅", value));
        }
        if (subscription.getExpireTime() != null && subscription.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new AccessDeniedException(String.format("【%s】服务订阅已过期", value));
        }

        return pjp.proceed();
    }

}

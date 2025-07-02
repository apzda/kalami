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
package com.apzda.kalami.boot.aop;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.kalami.data.domain.OrganizationAware;
import com.apzda.kalami.data.domain.TenantAware;
import com.apzda.kalami.tenant.TenantManager;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20) // 在SubscribedAuthorityAspect之后
@RequiredArgsConstructor
public class TenantAwareQueryAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController) && execution(public * *..controller.admin.*.*(..))")
    public Object proceed(@Nonnull ProceedingJoinPoint pjp) throws Throwable {
        for (Object argValue : pjp.getArgs()) {
            if (argValue instanceof TenantAware<?>) {
                BeanUtil.setFieldValue(argValue, "tenantId", TenantManager.tenantId("-1"));
            }

            if (argValue instanceof OrganizationAware<?>) {
                BeanUtil.setFieldValue(argValue, "orgId", TenantManager.orgId());
            }
        }

        return pjp.proceed();
    }

}

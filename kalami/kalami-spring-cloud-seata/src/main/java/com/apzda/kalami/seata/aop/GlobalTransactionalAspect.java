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
package com.apzda.kalami.seata.aop;

import com.apzda.kalami.annotation.GlobalTransactional;
import jakarta.annotation.Nonnull;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.core.exception.TransactionException;
import org.apache.seata.tm.api.GlobalTransaction;
import org.apache.seata.tm.api.GlobalTransactionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Aspect
@Component
@Order(0)
public class GlobalTransactionalAspect {

    @Around("@annotation(com.apzda.kalami.annotation.GlobalTransactional)")
    public Object handleGlobalTransaction(@Nonnull ProceedingJoinPoint joinPoint) throws Throwable {

        val signature = (MethodSignature) joinPoint.getSignature();
        val method = signature.getMethod();
        val trans = method.getAnnotation(GlobalTransactional.class);

        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();
        try {

            val name = trans.name();
            if (StringUtils.isNotBlank(name)) {
                tx.begin(trans.timeoutMills(), name);
            }
            else {
                tx.begin(trans.timeoutMills());
            }
            Object result = joinPoint.proceed();
            tx.commit();
            return result;
        }
        catch (Throwable ex) {
            handleRollback(tx, ex, trans.rollbackFor());
            throw ex;
        }
        finally {
            RootContext.unbind();
        }
    }

    private void handleRollback(@Nonnull GlobalTransaction tx, Throwable ex,
            @Nonnull Class<? extends Throwable>[] rollbackFor) throws TransactionException {
        boolean shouldRollback = true;

        if (rollbackFor.length > 0) {
            shouldRollback = false;
            for (Class<? extends Throwable> rbClass : rollbackFor) {
                if (rbClass.isAssignableFrom(ex.getClass())) {
                    shouldRollback = true;
                    break;
                }
            }
        }

        if (shouldRollback) {
            tx.rollback();
        }
        else {
            tx.commit(); // 不满足回滚条件则提交
        }
    }

}

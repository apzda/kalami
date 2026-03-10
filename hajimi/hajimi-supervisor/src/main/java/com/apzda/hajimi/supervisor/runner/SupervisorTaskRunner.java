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
package com.apzda.hajimi.supervisor.runner;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.apzda.hajimi.supervisor.AsyncTaskRunner;
import com.apzda.hajimi.supervisor.ISupervisor;
import com.apzda.hajimi.supervisor.Task;
import com.apzda.hajimi.supervisor.autoconfig.SupervisorConfigProperties;
import com.apzda.hajimi.supervisor.domain.entity.SupervisorTask;
import com.apzda.hajimi.supervisor.domain.service.ISupervisorTaskService;
import com.apzda.hajimi.supervisor.domain.vo.TaskStatus;
import com.apzda.kalami.exception.StopRetryException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Service
public class SupervisorTaskRunner implements AsyncTaskRunner, ApplicationListener<ApplicationReadyEvent> {

    private final ISupervisorTaskService supervisorTaskService;

    private final ObjectProvider<ISupervisor<?>> supervisorProvider;

    private final TransactionTemplate transactionTemplate;

    private final List<Duration> retries;

    private final ScheduledThreadPoolExecutor executor;

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    private final int executorCount;

    private final Map<String, ISupervisor<?>> supervisors = new HashMap<>();

    private final Duration delay;

    private volatile boolean running;

    public SupervisorTaskRunner(@Nonnull SupervisorConfigProperties config,
            @Nonnull ISupervisorTaskService supervisorTaskService, ObjectProvider<ISupervisor<?>> supervisorProvider,
            TransactionTemplate transactionTemplate) {
        this.supervisorTaskService = supervisorTaskService;
        this.supervisorProvider = supervisorProvider;
        this.transactionTemplate = transactionTemplate;
        this.retries = config.getRetries();
        this.delay = config.getDelay();

        val count = config.getCount();
        this.executorCount = count >= 1 ? count : Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

        this.executor = new ScheduledThreadPoolExecutor(executorCount, r -> {
            val thread = new Thread(r);
            thread.setName("supervisor-task-runner-" + atomicInteger.getAndAdd(1));
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void onApplicationEvent(@Nonnull ApplicationReadyEvent event) {
        supervisorProvider.orderedStream().forEach(supervisor -> {
            supervisors.put(supervisor.getName(), supervisor);
        });

        if (CollectionUtils.isEmpty(supervisors.values())) {
            log.debug("SupervisorTaskRunner: No supervisors found");
            return;
        }

        val seconds = delay.toSeconds();
        running = true;
        for (int i = 0; i < executorCount; i++) {
            executor.scheduleWithFixedDelay(new TaskRunner(this, i), 0, seconds, TimeUnit.SECONDS);
        }

        log.info("""
                监管者办公室已建立
                【线程】: {}
                【间隔】: {}秒
                【监管者】: {}""", executorCount, seconds, supervisors.keySet());
    }

    @PreDestroy
    public void stop() {
        this.running = false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public long run(@Nonnull String name, @Nonnull Runnable task) {
        return run(name, "", task);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public long run(@Nonnull String name, String content, @Nonnull Runnable task) {
        val asyncTask = new Task<>(name, content);
        val supervisorTask = supervisorTaskService.submit("", asyncTask, TaskStatus.RUNNING);

        CompletableFuture.runAsync(() -> {
            val rst = transactionTemplate.execute(status -> {
                try {
                    task.run();
                    log.debug("""
                            任务执行成功
                            【任务名】: {}
                            【执行者】: AsyncTaskRunner
                            【内容】: {}""", supervisorTask.getName(), supervisorTask.getContent());
                    return null;
                }
                catch (Exception ex) {
                    log.error("""
                            异步任务执行失败
                            【任务名】: {}
                            【执行者】: AsyncTaskRunner
                            【内容】: {}""", name, supervisorTask.getContent(), ex);
                    status.setRollbackOnly();
                    return ex.getMessage();
                }
            });
            val st = new SupervisorTask();
            st.setId(supervisorTask.getId());
            st.setSharding(supervisorTask.getSharding());
            st.setStatus(TaskStatus.DONE);
            if (rst != null) {
                st.setStatus(TaskStatus.FAIL);
                st.setRetries(Integer.MAX_VALUE);
                st.setRemark(rst);
            }

            transactionTemplate.executeWithoutResult(status -> {
                supervisorTaskService.updateStatus(st, TaskStatus.RUNNING);
            });
        });

        return supervisorTask.getId();
    }

    static class TaskRunner implements Runnable {

        private final SupervisorTaskRunner parent;

        private final ISupervisorTaskService taskService;

        private final Map<String, ISupervisor<?>> supervisors;

        private final int sharding;

        TaskRunner(@Nonnull SupervisorTaskRunner parent, int sharding) {
            this.parent = parent;
            this.taskService = parent.supervisorTaskService;
            this.supervisors = parent.supervisors;
            this.sharding = sharding;
        }

        @Override
        public void run() {
            try {
                SupervisorTask task;
                do {
                    task = taskService.getByStatusAndRunAtLe(TaskStatus.PENDING,
                            LocalDateTime.now().toEpochSecond(ZoneOffset.UTC), this.sharding);
                    if (task != null) {
                        runTask(task);
                    }
                }
                while (parent.running && task != null);
            }
            catch (Exception e) {
                log.warn("SupervisorTaskRunner: Run task error: {}", e.getMessage(), e);
            }
        }

        void runTask(SupervisorTask task) {
            try {
                task.setStatus(TaskStatus.RUNNING);
                if (!taskService.updateStatus(task, TaskStatus.PENDING)) {
                    // 乐观锁
                    return;
                }
                val content = task.getContent();
                if (!StringUtils.hasText(content)) {
                    throw new StopRetryException("工作负载为空");
                }
                if (!StringUtils.hasText(task.getSupervisor())) {
                    throw new StopRetryException("任务的执行者未指定");
                }
                val runner = supervisors.get(task.getSupervisor());
                if (runner == null) {
                    log.warn("""
                            任务执行失败
                            【任务名】: {}
                            【执行者】: {}
                            【内容】: {}
                            【是否重试】: 否""", task.getName(), task.getSupervisor(), task.getContent());
                    throw new StopRetryException("监督者不存在:" + task.getSupervisor());
                }

                val result = runner.check(content);

                task.setStatus(TaskStatus.DONE);
                if (!result) {
                    log.warn("""
                            任务执行失败
                            【任务名】: {}
                            【执行者】: {}
                            【内容】: {}
                            【是否重试】: 否""", task.getName(), task.getSupervisor(), task.getContent());
                    throw new StopRetryException("任务执行结果失败");
                }

                log.debug("""
                        任务执行成功
                        【任务名】: {}
                        【执行者】: {}
                        【内容】: {}""", task.getName(), task.getSupervisor(), task.getContent());
                try {
                    taskService.removeById(task);
                }
                catch (Exception e) {
                    if (!taskService.updateStatus(task, TaskStatus.RUNNING)) {
                        log.warn("""
                                无法更新任务状态
                                【任务名】: {}
                                【执行者】: {}
                                【内容】: {}
                                【是否重试】: 否""", task.getName(), task.getSupervisor(), task.getContent());
                        throw new StopRetryException("无法更新任务状态");
                    }
                }
            }
            catch (Exception e) {
                val message = ExceptionUtil.getSimpleMessage(ExceptionUtil.getRootCause(e));
                val currentRetry = task.getRetries();
                val retries = parent.retries;
                if (!(e instanceof StopRetryException) && retries.size() >= (currentRetry + 1)) {
                    task.setStatus(TaskStatus.PENDING);
                    task.setRetries(currentRetry + 1);
                    val duration = retries.get(currentRetry);
                    task.setRunAt(System.currentTimeMillis() + duration.toMillis());
                    log.warn("""
                            任务执行异常
                            【任务名】: {}
                            【执行者】: {}
                            【内容】: {}
                            【是否重试】: 是
                            【异常】: {}""", task.getName(), task.getSupervisor(), task.getContent(), e.getMessage());
                }
                else {
                    task.setStatus(TaskStatus.FAIL);
                    if (!(e instanceof StopRetryException)) {
                        log.warn("""
                                任务执行异常
                                【任务名】: {}
                                【执行者】: {}
                                【内容】: {}
                                【是否重试】: 否
                                【异常】: {}""", task.getName(), task.getSupervisor(), task.getContent(), e.getMessage());
                    }
                }
                task.setRemark(message);
                taskService.updateStatus(task, TaskStatus.RUNNING);
            }
        }

    }

}

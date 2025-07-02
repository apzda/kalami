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
package com.apzda.kalami.boot.controller;

import com.apzda.kalami.boot.query.QueryGenerator;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.domain.AuditLog;
import com.apzda.kalami.data.domain.IEntity;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.error.ResourceNotFoundError;
import com.apzda.kalami.event.AuditEvent;
import com.apzda.kalami.exception.BizException;
import com.apzda.kalami.i18n.I18n;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.user.CurrentUserProvider;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.time.Clock;
import java.util.Collections;
import java.util.List;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
public abstract class AbstractCrudController<D extends Serializable, T extends IEntity<D>, S extends IService<T>>
        implements ApplicationContextAware, InitializingBean {

    protected ApplicationEventPublisher eventPublisher;

    private Clock clock;

    private S serviceImpl;

    protected String resourceId;

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        val resolvableType = ResolvableType.forClass(AbstractCrudController.class, this.getClass());
        Class<T> entityClz = (Class<T>) resolvableType.getGeneric(1).resolve();
        Class<S> serviceClz = (Class<S>) resolvableType.getGeneric(2).resolve();
        assert entityClz != null && serviceClz != null;
        this.serviceImpl = applicationContext.getBean(serviceClz);
        this.resourceId = StringUtils.uncapitalize(String.format("{resource.%s}", entityClz.getSimpleName()));
        try {
            this.eventPublisher = applicationContext.getBean(ApplicationEventPublisher.class);
        }
        catch (BeansException ignored) {
        }
        try {
            this.clock = applicationContext.getBean(Clock.class);
        }
        catch (BeansException ignored) {
        }
    }

    /**
     * 分页查询列表.
     * @param entity 查询实体
     * @param page 分页
     * @param req 原生请求
     * @return 查询结果
     */
    protected Response<Paged<T>> queryPage(T entity, Pageable page, HttpServletRequest req) {
        QueryWrapper<T> query = alter(QueryGenerator.initQueryWrapper(entity, req.getParameterMap()), entity, req);
        query.checkSqlInjection();

        val result = this.serviceImpl.page(PageUtil.from(page), query);

        return Response.success(PageUtil.from(result));
    }

    /**
     * 获取实体实例.
     * @param id 实体ID
     * @return 实体实例
     */
    protected Response<T> findById(D id) {
        val entity = this.serviceImpl.getById(id);
        if (entity == null) {
            new ResourceNotFoundError(this.resourceId, String.valueOf(id)).emit();
        }
        return Response.success(entity);
    }

    /**
     * 新增实体.
     * @param entity 要新增的实体实例.
     * @return 新增后的实体实例.
     */
    protected Response<T> create(@Validated(Group.New.class) @NotNull T entity) {
        val audit = new AuditLog();
        audit.setActivity("Create Entity");
        audit.setTemplate(true);
        try {
            audit.setNewValue(entity);
            if (this.serviceImpl.save(alter(entity))) {
                audit.setOldValue(entity);
                audit.setNewValue(entity);
                audit.setMessage("entity created successfully");
                return Response.success(entity);
            }
            audit.setMessage("entity not created: {}");
            audit.setLevel("warn");
            audit.getArgs().add(I18n.t("error.995"));
            return Response.error(-995);
        }
        catch (Exception ae) {
            audit.setMessage("entity not created: {}");
            audit.setLevel("warn");
            audit.getArgs().add(ae.getMessage());
            throw ae;
        }
        finally {
            this.publishAuditEvent(audit);
        }
    }

    /**
     * 修改实体.
     * @param id 实体ID
     * @param entity 新的实体实例.
     * @return 修改后的实体实例.
     */
    protected Response<T> update(D id, @Validated(Group.Update.class) @NotNull T entity) {
        val audit = new AuditLog();
        audit.setActivity("Update Entity");
        audit.setTemplate(true);
        try {
            audit.setNewValue(entity);
            val o = serviceImpl.getById(id);
            if (o == null) {
                throw new BizException(new ResourceNotFoundError(resourceId, String.valueOf(id)));
            }
            audit.setOldValue(o);
            val altered = alter(o, entity);
            altered.setId(id);
            if (this.serviceImpl.updateById(altered)) {
                audit.setMessage("entity updated successfully");
                return Response.success(serviceImpl.getById(id));
            }
            audit.setMessage("entity not updated: {}");
            audit.setLevel("warn");
            audit.getArgs().add(I18n.t("error.995"));
            return Response.error(-995);
        }
        catch (Exception ae) {
            audit.setMessage("entity not updated: {}");
            audit.setLevel("warn");
            audit.getArgs().add(ae.getMessage());
            throw ae;
        }
        finally {
            publishAuditEvent(audit);
        }
    }

    /**
     * 删除实体.
     * @param id 实体ID.
     * @return 被删除的实体.
     */
    protected Response<T> delete(D id) {
        val audit = new AuditLog();
        audit.setActivity("Delete Entity");
        audit.setTemplate(true);
        try {
            val o = this.serviceImpl.getById(id);
            if (o == null) {
                new ResourceNotFoundError(this.resourceId, String.valueOf(id)).emit();
            }
            audit.setOldValue(o);
            if (this.serviceImpl.removeById(id)) {
                audit.setMessage("entity deleted successfully");
                return Response.success(o);
            }
            audit.setMessage("entity not deleted: {}");
            audit.setLevel("warn");
            audit.getArgs().add(I18n.t("error.995"));
            return Response.error(-995);
        }
        catch (Exception ae) {
            audit.setMessage("entity not deleted: {}");
            audit.setLevel("warn");
            audit.getArgs().add(ae.getMessage());
            throw ae;
        }
        finally {
            this.publishAuditEvent(audit);
        }
    }

    /**
     * 批量删除实体.
     * @param ids 要删除的实体ID列表
     * @return 被删除的实体列表
     */
    protected Response<List<T>> delete(List<D> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Response.success(Collections.emptyList());
        }
        val entities = this.serviceImpl.listByIds(ids);
        if (CollectionUtils.isEmpty(entities)) {
            return Response.success(Collections.emptyList());
        }

        val audit = new AuditLog();
        audit.setActivity("Delete Entity");
        audit.setTemplate(true);
        try {
            audit.setOldValue(entities);
            if (this.serviceImpl.removeByIds(entities)) {
                audit.setMessage("entity deleted successfully");
                return Response.success(entities);
            }

            audit.setMessage("entity not deleted: {}");
            audit.setLevel("warn");
            audit.getArgs().add(I18n.t("error.995"));
            return Response.error(-995);
        }
        catch (Exception ae) {
            audit.setMessage("entity not deleted: {}");
            audit.setLevel("warn");
            audit.getArgs().add(ae.getMessage());
            throw ae;
        }
        finally {
            this.publishAuditEvent(audit);
        }
    }

    /**
     * 对即将新增的实体进行调整.
     * @param entity 即将新增的实体
     * @return 调整后的实体
     */
    @Nonnull
    protected T alter(T entity) {
        return entity;
    }

    /**
     * 对即将修改的实体进行调整.
     * @param old 原实体
     * @param entity 新实体
     * @return 调整后的实体
     */
    @Nonnull
    protected T alter(@Nonnull T old, @Nonnull T entity) {
        return entity;
    }

    @Nonnull
    protected QueryWrapper<T> alter(QueryWrapper<T> query, T entity, HttpServletRequest req) {
        return query;
    }

    protected final void publishAuditEvent(@Nonnull AuditLog audit) {
        audit.setTarget(this.resourceId);
        audit.setRunas(CurrentUserProvider.getCurrentUser().getRunAs());

        if (this.eventPublisher != null) {
            if (this.clock == null) {
                this.eventPublisher.publishEvent(new AuditEvent(audit));
            }
            else {
                this.eventPublisher.publishEvent(new AuditEvent(audit, clock));
            }

            log.debug("AuditEvent published: {}", audit);
        }
    }

}

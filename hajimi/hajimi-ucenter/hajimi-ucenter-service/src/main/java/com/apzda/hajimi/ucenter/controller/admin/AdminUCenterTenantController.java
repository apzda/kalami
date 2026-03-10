/*
 * Copyright 2025-2026 the original author or authors.
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

package com.apzda.hajimi.ucenter.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.hajimi.auditor.Audit;
import com.apzda.hajimi.auditor.AuditContextHolder;
import com.apzda.hajimi.supervisor.AsyncTaskRunner;
import com.apzda.hajimi.ucenter.controller.admin.dto.SubscriptionDto;
import com.apzda.hajimi.ucenter.controller.admin.dto.SubscriptionVo;
import com.apzda.hajimi.ucenter.controller.admin.dto.TenantPageQuery;
import com.apzda.hajimi.ucenter.controller.admin.dto.TenantUserPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.TenantEntity;
import com.apzda.hajimi.ucenter.domain.entity.TenantSubscriptionEntity;
import com.apzda.hajimi.ucenter.domain.entity.UserAccountTransactionEntity;
import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.hajimi.ucenter.domain.repository.OrganizationRepository;
import com.apzda.hajimi.ucenter.domain.repository.TenantRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.domain.vo.TenantUserVo;
import com.apzda.hajimi.ucenter.domain.vo.TenantVo;
import com.apzda.hajimi.ucenter.event.TenantDisabledEvent;
import com.apzda.hajimi.ucenter.event.TenantEnabledEvent;
import com.apzda.hajimi.ucenter.infrastructure.converter.UCenterConverter;
import com.apzda.hajimi.ucenter.service.dto.SubscribeDto;
import com.apzda.hajimi.ucenter.service.dto.TenantDto;
import com.apzda.hajimi.ucenter.service.dto.TransQueryDto;
import com.apzda.hajimi.ucenter.service.vo.AccountSummaryVo;
import com.apzda.hajimi.ucenter.service.vo.TransactionVo;
import com.apzda.kalami.annotation.Dictionary;
import com.apzda.kalami.data.Paged;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.data.Response;
import com.apzda.kalami.data.dto.IDS;
import com.apzda.kalami.data.validation.Group;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.tenant.Subscription;
import com.apzda.kalami.tenant.Tenant;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/wk/uc/tenant")
@RequiredArgsConstructor
public class AdminUCenterTenantController {

    private final TenantRepository tenantRepository;

    private final UCenterConverter userConverter;

    private final UserRepository userRepository;

    protected final UserAccountRepository userAccountRepository;

    protected final OrganizationRepository organizationRepository;

    protected final UCenterConverter ucenterConverter;

    private final AsyncTaskRunner asyncTaskRunner;

    private final ApplicationEventPublisher publisher;

    /**
     * 租户列表
     */
    @GetMapping("/list")
    @PreAuthorize("@authz.iCan('r:core.tenant')")
    @Dictionary
    public Response<Paged<TenantVo>> list(TenantPageQuery query, QuickSearch quickSearch) {
        val tenantId = TenantManager.currentTenantId("0");
        query.setParentId(-1L);
        if ("0".equals(tenantId)) {
            query.setParentId(0L);
        }
        else {
            val tenant = tenantRepository.getById(tenantId);
            if (tenant != null) {
                query.setParentId(tenant.getId());
            }
        }

        IPage<TenantEntity> tenants = tenantRepository.pageQuery(query, quickSearch);
        tenants.setRecords(tenants.getRecords().stream().filter(tenantEntity -> tenantEntity.getId() != 0).toList());
        return Response.success(PageUtil.from(tenants, userConverter::fromTenants));
    }

    /**
     * 租户自动完成
     */
    @GetMapping("/autoComplete")
    @PreAuthorize("@authz.iCan('r:core.tenant')")
    public Response<List<TenantVo>> autoComplete(@RequestParam(required = false) String q,
            @RequestParam(required = false) String value) {
        val query = new TenantPageQuery();
        query.searchCount(false);
        query.setPageNumber(1);
        query.setPageSize(20);

        val tenantId = TenantManager.currentTenantId("0");
        query.setParentId(-1L);
        if ("0".equals(tenantId)) {
            query.setParentId(0L);
        }
        else {
            val tenant = tenantRepository.getById(tenantId);
            if (tenant != null) {
                query.setParentId(tenant.getId());
            }
        }

        if (StringUtils.isNotBlank(value)) {
            query.setId(value);
        }
        else if (StringUtils.isNotBlank(q)) {
            query.setName(q);
        }

        IPage<TenantEntity> tenants = tenantRepository.pageQuery(query, new QuickSearch());
        tenants.setRecords(tenants.getRecords().stream().filter(tenantEntity -> tenantEntity.getId() != 0).toList());
        return Response.success(userConverter.fromTenants(tenants.getRecords()));
    }

    /**
     * 租户用户列表
     */
    @GetMapping("/users")
    @PreAuthorize("@authz.iCan('r:core.tenant')")
    @Dictionary
    public Response<Paged<TenantUserVo>> users(TenantUserPageQuery query, QuickSearch quickSearch) {
        IPage<TenantUserVo> tenants = tenantRepository.users(query, quickSearch);
        return Response.success(PageUtil.from(tenants));
    }

    /**
     * 创建租户
     */
    @PostMapping("/create")
    @PreAuthorize("@authz.iCan('c:core.tenant')")
    @Audit(activity = "创建租户", target = "#{#dto.name}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> create(@RequestBody @Validated({ Group.New.class, Group.Default.class }) TenantDto dto) {
        dto.setTenantId(TenantManager.currentTenantId("0"));
        if (tenantRepository.create(dto) != null) {
            return Response.success(true).notify(String.format("租户'%s'创建成功", dto.getName()));
        }
        else {
            return Response.error("创建租户失败");
        }
    }

    /**
     * 更新租户
     */
    @PostMapping("/update")
    @PreAuthorize("@authz.iCan('u:core.tenant')")
    @Audit(activity = "修改租户", target = "#{#dto.id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> update(
            @RequestBody @Validated({ Group.Update.class, Group.Default.class }) TenantDto dto) {
        val tenant = tenantRepository.getById(dto.getId());
        if (tenant == null) {
            return Response.error("租户不存在");
        }

        if (!TenantManager.isMyTenant(tenant.getParentId())) {
            return Response.error("更新失败[无权限]");
        }

        val context = AuditContextHolder.getContext();
        context.setOldValue(tenant);
        context.setNewValue(dto);

        if (tenantRepository.update(dto) != null) {
            return Response.success(true).notify(String.format("租户'%s'修改成功", dto.getName()));
        }
        else {
            return Response.error("租户修改失败");
        }
    }

    /**
     * 系统可用订阅服务
     */
    @GetMapping("/available/subscriptions")
    @PreAuthorize("@authz.iCan('r:core.tenant')")
    public Response<Collection<Subscription>> availableSubscriptions() {
        return Response.success(TenantManager.availableSubscriptions().values());
    }

    /**
     * 租户订阅列表
     */
    @GetMapping("/subscriptions/{id}")
    @PreAuthorize("@authz.iCan('r:core.tenant')")
    public Response<SubscriptionVo> subscriptions(@PathVariable String id) {
        val tenant = tenantRepository.getById(id);
        if (tenant == null) {
            return Response.error("租户不存在");
        }

        if (!TenantManager.isMyTenant(tenant.getParentId())) {
            return Response.error("无权查看订阅列表");
        }
        val availableSubscriptions = TenantManager.availableSubscriptions();
        val vo = new SubscriptionVo();
        val tenantId = TenantManager.currentTenantId();
        if ("0".equals(tenantId)) {
            vo.setAvailable(availableSubscriptions.values());
        }
        else {
            // 只显示自己订阅的服务
            val mySubscriptions = tenantRepository.findSubscriptionsById(tenantId);
            if (mySubscriptions.isEmpty()) {
                vo.setAvailable(List.of());
            }
            else {
                List<Subscription> subs = mySubscriptions.stream()
                    .map(subscription -> availableSubscriptions.get(subscription.getService()))
                    .toList();
                vo.setAvailable(subs);
            }
        }

        val subMap = new HashMap<String, SubscriptionDto>();
        val subscriptions = tenantRepository.findSubscriptionsById(id);
        subscriptions.forEach(subscription -> {
            subMap.put(subscription.getService(), BeanUtil.copyProperties(subscription, SubscriptionDto.class));
        });
        vo.setServices(subMap);

        return Response.success(vo);
    }

    /**
     * 订阅服务
     */
    @PostMapping("/subscriptions/{id}")
    @PreAuthorize("@authz.iCan('u:core.tenant')")
    public Response<Boolean> subscribe(@PathVariable String id,
            @RequestBody Map<String, SubscriptionDto> subscriptions) {
        val tenant = tenantRepository.getById(id);
        if (tenant == null) {
            return Response.error("租户不存在");
        }

        if (!TenantManager.isMyTenant(tenant.getParentId())) {
            return Response.error("订阅调整失败[无权限]");
        }

        val tenantId = TenantManager.currentTenantId();
        val mySubscriptions = new HashMap<String, TenantSubscriptionEntity>();

        if (!"0".equals(tenantId)) {
            tenantRepository.findSubscriptionsById(tenantId).forEach(subscription -> {
                mySubscriptions.put(subscription.getService(), subscription);
            });
        }

        subscriptions.forEach((service, subscription) -> {
            if (Boolean.FALSE.equals(subscription.getExpired())) {
                val dto = new SubscribeDto();
                dto.setTenantId(id);
                dto.setService(service);
                if ("0".equals(tenantId)) {
                    dto.setExpireTime(subscription.getExpiredTime());
                }
                else {
                    if (!mySubscriptions.containsKey(service)) {
                        // 直接取消订阅
                        tenantRepository.unsubscribe(id, service);
                        return;
                    }

                    val mySubscription = mySubscriptions.get(service);
                    if (dto.getExpireTime() == null) {
                        // 未指定订阅时间时以当前租户的为准
                        dto.setExpireTime(mySubscription.getExpiredTime());
                    }
                    else if (mySubscription.getExpiredTime() != null
                            && mySubscription.getExpiredTime().isBefore(dto.getExpireTime())) {
                        // 指定了订阅过期时间且过期时间晚于当前租户的过期时间，以当前租户的过期时间为准
                        dto.setExpireTime(mySubscription.getExpiredTime());
                    }
                }

                if (dto.getExpireTime() == null) {
                    dto.setExpired(0);
                }

                tenantRepository.subscribe(dto);
            }
            else {
                tenantRepository.unsubscribe(id, service);
            }
        });

        return Response.success(true);
    }

    /**
     * 禁用租户
     */
    @PostMapping("/disable/{id}")
    @PreAuthorize("@authz.iCan('u:core.tenant')")
    @Audit(activity = "禁用租户", target = "#{#id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> disable(@PathVariable String id) {
        val tenant = tenantRepository.getById(id);
        if (tenant == null) {
            return Response.error("租户不存在");
        }
        if (tenant.getId() == 0) {
            return Response.error("不能禁用系统租户");
        }
        if (!TenantManager.isMyTenant(tenant.getParentId())) {
            return Response.error("不能禁用[无权限]");
        }
        tenant.setStatus(TenantStatus.DISABLED);
        tenantRepository.updateById(tenant);

        val source = tenant.toTenant();
        source.setStatus(TenantStatus.DISABLED.name());
        val event = new TenantDisabledEvent(source);
        publisher.publishEvent(event);
        return Response.success(true).notify(String.format("租户'%s'禁用成功", tenant.getName()));
    }

    /**
     * 启用租户
     */
    @PostMapping("/enable/{id}")
    @PreAuthorize("@authz.iCan('u:core.tenant')")
    @Audit(activity = "启用租户", target = "#{#id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> enable(@PathVariable String id) {
        val tenant = tenantRepository.getById(id);
        if (tenant == null) {
            return Response.error("租户不存在");
        }
        if (tenant.getId() == 0) {
            return Response.error("不能启用系统租户");
        }
        if (!TenantManager.isMyTenant(tenant.getParentId())) {
            return Response.error("不能启用[无权限]");
        }
        tenant.setStatus(TenantStatus.ENABLED);
        tenantRepository.updateById(tenant);
        val source = new Tenant();
        source.setId(String.valueOf(tenant.getId()));
        source.setCode(tenant.getCode());
        source.setName(tenant.getName());
        source.setShortName(tenant.getShortName());
        source.setLogo(tenant.getLogo());
        source.setUid(String.valueOf(tenant.getUid()));
        source.setCurrent(false);
        source.setStatus(TenantStatus.ENABLED.name());
        val event = new TenantEnabledEvent(source);
        publisher.publishEvent(event);
        return Response.success(true).notify(String.format("租户'%s'启用成功", tenant.getName()));
    }

    /**
     * 转移所有权
     */
    @PostMapping("/transform/{id}/{uid}")
    @PreAuthorize("@authz.iCan('u:core.tenant')")
    @Audit(activity = "转移所有权", target = "#{#id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> transform(@PathVariable String id, @PathVariable String uid) {
        val tenant = tenantRepository.getById(id);
        if (tenant == null) {
            return Response.error("租户不存在");
        }
        if (tenant.getId() == 0) {
            return Response.error("系统所有权不可转移");
        }
        if (!TenantManager.isMyTenant(tenant.getParentId())) {
            return Response.error("所有权不可转移[无权限]");
        }
        val context = AuditContextHolder.getContext();
        context.setOldValue(tenant.getUid());
        context.setNewValue(uid);

        val user = userRepository.getById(uid);
        if (user == null) {
            return Response.error("用户不存在");
        }

        tenant.setUid(user.getId());
        if (!tenantRepository.updateById(tenant)) {
            return Response.error("所有权移交失败");
        }

        if (tenantRepository.join(tenant.getId(), user.getId()) == null) {
            throw new IllegalStateException(String.format("用户'%s'加入租户失败", user.getNickname()));
        }

        return Response.success(true).notify(String.format("租户所有者已经移交给'%s'", user.getNickname()));
    }

    /**
     * 解绑用户
     */
    @PostMapping("/dismiss/{id}/{uid}")
    @PreAuthorize("@authz.iCan('u:core.tenant')")
    @Audit(activity = "解绑用户", target = "#{#id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Boolean> dismiss(@PathVariable String id, @PathVariable Long uid) {
        val tenant = tenantRepository.getById(id);
        if (tenant == null) {
            return Response.error("租户不存在");
        }
        if (tenant.getId() == 0) {
            return Response.error("不能解绑系统用户");
        }
        if (!TenantManager.isMyTenant(tenant.getParentId())) {
            return Response.error("不能解绑系统用户[无权限]");
        }
        val context = AuditContextHolder.getContext();
        context.setNewValue(uid);

        val user = userRepository.getById(uid);
        if (user == null) {
            return Response.error("用户不存在");
        }

        if (Objects.equals(tenant.getUid(), uid)) {
            throw new CommonBizException(String.format("'%s'为当前租户的超级管理员，不能解绑.", user.getNickname()));
        }

        if (!tenantRepository.leave(tenant.getId(), uid)) {
            return Response.error(String.format("用户'%s'已与租户'%s'解绑失败", user.getNickname(), tenant.getName()));
        }

        return Response.success(true).notify(String.format("用户'%s'已与租户'%s'解绑", user.getNickname(), tenant.getName()));
    }

    /**
     * 同步权限
     */
    @PostMapping("/sync")
    @PreAuthorize("@authz.iCan('u:core.tenant')")
    @Audit(activity = "同步权限",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    public Response<Boolean> sync(@RequestBody IDS ids) {
        val tenantIds = ids.getIds();
        if (CollectionUtils.isEmpty(tenantIds)) {
            return Response.success(true).notify("已同步");
        }

        val taskId = asyncTaskRunner.run("同步权限", Joiner.on(",").join(tenantIds), () -> {
            tenantIds.forEach(id -> {
                val tenant = tenantRepository.getById(id);
                if (tenant == null || tenant.getId() == 0) {
                    return;
                }
                if (!TenantManager.isMyTenant(tenant.getParentId())) {
                    return;
                }
                tenantRepository.syncPrivileges(tenant);
            });
        });

        return Response.success(true).notify("权限同步任务已提交，任务ID: " + taskId);
    }

    /**
     * 当前租户信息
     */
    @GetMapping("/info")
    @PreAuthorize("@authz.iCan('r:core.tenant')")
    public Response<TenantVo> info() {
        val tenantId = TenantManager.currentTenantId("0");
        val tenant = tenantRepository.getById(tenantId);
        if (tenant == null) {
            return Response.error("租户不存在");
        }

        return Response.success(userConverter.from(tenant));
    }

    /**
     * 设置租户信息
     */
    @PostMapping("/setting")
    @PreAuthorize("@authz.iCan('u:core.tenant')")
    @Audit(activity = "修改租户", target = "#{#dto.id}",
            message = "#{isTrue(#returnObj.data)?'成功':any(#returnObj?.errMsg,#throwObj?.message,'失败')}")
    @Transactional
    public Response<Tenant> updateTenant(
            @RequestBody @Validated({ Group.Update.class, Group.Default.class }) TenantDto dto) {
        dto.setId(Long.parseLong(TenantManager.currentTenantId("0")));
        dto.setStatus(null);
        dto.setCode(null);

        val tenant = tenantRepository.getById(dto.getId());
        if (tenant == null) {
            return Response.error("租户不存在");
        }

        val context = AuditContextHolder.getContext();
        context.setOldValue(BeanUtil.copyProperties(tenant, TenantEntity.class));

        if (tenantRepository.update(dto) != null) {
            val nt = tenantRepository.getById(dto.getId());
            context.setNewValue(nt);
            val t = nt.toTenant();

            return Response.success(t);
        }
        else {
            return Response.error("租户修改失败");
        }
    }

    /**
     * 当前租户资产交易列表
     */
    @GetMapping("/account/trans")
    @Dictionary
    public Response<List<TransactionVo>> trans(TransQueryDto req, CurrentUser user) {
        val tenantId = "0";
        req.searchCount(false);
        if (StringUtils.isBlank(req.getPageSorts())) {
            req.setPageSorts("createdAt|DESC");
        }
        val tid = TenantManager.currentTenantId("0");
        var uid = user.getUid();
        if (!"0".equals(tid)) {
            val tenant = tenantRepository.getById(Long.parseLong(tid));
            if (tenant != null) {
                uid = String.valueOf(tenant.getUid());
            }
        }

        IPage<UserAccountTransactionEntity> page = userAccountRepository.pageQueryTransactions(req, tenantId, uid);
        val trans = page.getRecords();

        return Response.success(this.ucenterConverter.fromTransactions(trans));
    }

    /**
     * 当前租户账户总览
     */
    @GetMapping("/account/assets")
    public Response<AccountSummaryVo> assets(CurrentUser user) {
        val tenantId = "0";
        val tid = TenantManager.currentTenantId("0");
        var uid = user.getUid();
        if (!"0".equals(tid)) {
            val tenant = tenantRepository.getById(Long.parseLong(tid));
            if (tenant != null) {
                uid = String.valueOf(tenant.getUid());
            }
        }
        val res = userAccountRepository.summary(tenantId, uid, List.of());
        return Response.success(res);
    }

}

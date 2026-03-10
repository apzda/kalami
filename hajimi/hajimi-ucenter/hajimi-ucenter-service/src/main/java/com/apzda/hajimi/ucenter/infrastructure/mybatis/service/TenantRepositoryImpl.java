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

package com.apzda.hajimi.ucenter.infrastructure.mybatis.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.apzda.hajimi.ucenter.config.UCenterTenantConfigureProperties;
import com.apzda.hajimi.ucenter.controller.admin.dto.TenantPageQuery;
import com.apzda.hajimi.ucenter.controller.admin.dto.TenantUserPageQuery;
import com.apzda.hajimi.ucenter.domain.entity.*;
import com.apzda.hajimi.ucenter.domain.enums.TenantStatus;
import com.apzda.hajimi.ucenter.domain.enums.UserStatus;
import com.apzda.hajimi.ucenter.domain.repository.PrivilegeRepository;
import com.apzda.hajimi.ucenter.domain.repository.RoleRepository;
import com.apzda.hajimi.ucenter.domain.repository.TenantRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserRepository;
import com.apzda.hajimi.ucenter.domain.vo.TenantUserVo;
import com.apzda.hajimi.ucenter.event.TenantCreatedEvent;
import com.apzda.hajimi.ucenter.event.TenantModifiedEvent;
import com.apzda.hajimi.ucenter.event.TenantSwitchedEvent;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.TenantMapper;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.TenantSubscriptionMapper;
import com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper.TenantUserMapper;
import com.apzda.hajimi.ucenter.service.dto.SubscribeDto;
import com.apzda.hajimi.ucenter.service.dto.TenantDto;
import com.apzda.hajimi.ucenter.service.dto.UserDto;
import com.apzda.hajimi.ucenter.tenant.DefaultSubscription;
import com.apzda.kalami.context.KalamiContextHolder;
import com.apzda.kalami.data.QuickSearch;
import com.apzda.kalami.error.DuplicateError;
import com.apzda.kalami.error.ResourceCreateError;
import com.apzda.kalami.error.ResourceNotFoundError;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.mybatisplus.utils.PageUtil;
import com.apzda.kalami.service.CounterService;
import com.apzda.kalami.service.SerialNumberGenerator;
import com.apzda.kalami.tenant.Tenant;
import com.apzda.kalami.tenant.TenantManager;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantRepositoryImpl extends ServiceImpl<TenantMapper, TenantEntity>
        implements TenantRepository, SerialNumberGenerator {

    private final TenantUserMapper tenantUserMapper;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final TenantSubscriptionMapper tenantSubscriptionMapper;

    private final PrivilegeRepository privilegeRepository;

    private final UCenterTenantConfigureProperties properties;

    private final CounterService counterService;

    private final ApplicationEventPublisher publisher;

    @Override
    public String generate(String prefix, int length, String padding) {
        val sn = counterService.count("TENANT.SN", 1);

        return String.format("%s%s%s", prefix, DateUtil.currentSeconds(), StringUtils.leftPad(sn + "", 3, padding));
    }

    @Override
    public boolean support(String type) {
        return "TENANT.SN".equals(type);
    }

    @Override
    public TenantEntity findByCode(String code) {
        return lambdaQuery().eq(TenantEntity::getCode, code).one();
    }

    @Override
    public IPage<TenantEntity> pageQuery(@Nonnull TenantPageQuery query, @Nonnull QuickSearch quickSearch) {
        IPage<TenantEntity> pager = PageUtil.from(query);
        if (quickSearch.isEnabled()) {
            query.setName(quickSearch.getKeyword());
        }
        final String inSql;
        if (!CollectionUtils.isEmpty(query.getServices())) {
            inSql = "SELECT tenant_id FROM ucenter_tenant_subscription WHERE service IN ('"
                    + Joiner.on("','").join(query.getServices()) + "') GROUP BY tenant_id";
            SqlInjectionUtils.check(inSql);
        }
        else {
            inSql = null;
        }

        return this.lambdaQuery()
            .eq(TenantEntity::getParentId, query.getParentId())
            .like(StringUtils.isNotBlank(query.getName()), TenantEntity::getName, query.getName())
            .eq(query.getStatus() != null, TenantEntity::getStatus, query.getStatus())
            .eq(query.getAgentId() != null, TenantEntity::getAgentId, query.getAgentId())
            .eq(StringUtils.isNotBlank(query.getCode()), TenantEntity::getCode, query.getCode())
            .eq(StringUtils.isNotBlank(query.getPhone()), TenantEntity::getPhone, query.getPhone())
            .eq(StringUtils.isNotBlank(query.getId()), TenantEntity::getId, query.getId())
            .eq(StringUtils.isNotBlank(query.getUid()), TenantEntity::getUid, query.getUid())
            .ge(query.getCreateStartTime() != null, TenantEntity::getCreatedAt, query.getCreateStartTime())
            .le(query.getCreateEndTime() != null, TenantEntity::getCreatedAt, query.getCreateEndTime())
            .inSql(inSql != null, TenantEntity::getId, inSql) // TBD 是否需要换一个性能更好的方法
            .page(pager);
    }

    @Override
    public IPage<TenantUserVo> users(@Nonnull TenantUserPageQuery query, @Nonnull QuickSearch quickSearch) {
        IPage<TenantUserVo> pager = PageUtil.from(query);
        if (quickSearch.isEnabled()) {
            query.setName(quickSearch.getKeyword());
        }

        return tenantUserMapper.users(pager, query);
    }

    @Override
    @Cacheable(cacheNames = "uc:tn:users", key = "#uid")
    public List<TenantUserVo> listTenantByUid(String uid) {
        val query = new TenantUserPageQuery();
        query.setUid(uid);
        query.setStatus(TenantStatus.ENABLED);
        query.setPageNumber(1);
        query.setPageSize(500);
        query.setPageSorts("current|DESC");
        IPage<TenantUserVo> pager = PageUtil.from(query, false);
        return tenantUserMapper.users(pager, query).getRecords();
    }

    @Override
    @Cacheable(cacheNames = "uc:tn:subscription", key = "#tenantId")
    public List<DefaultSubscription> listSubscriptionsById(String tenantId) {
        val availableSubscriptions = TenantManager.availableSubscriptions();
        val where = Wrappers.lambdaQuery(TenantSubscriptionEntity.class)
            .eq(TenantSubscriptionEntity::getTenantId, tenantId)
            .eq(TenantSubscriptionEntity::getExpired, false);

        return tenantSubscriptionMapper.selectList(where)
            .stream()
            .filter(ts -> availableSubscriptions.containsKey(ts.getService()))
            .map(ts -> {
                DefaultSubscription defaultSubscription = new DefaultSubscription();
                val sub = availableSubscriptions.get(ts.getService());
                defaultSubscription.setId(ts.getService());
                defaultSubscription.setName(sub.getName());
                defaultSubscription.setLogo(sub.getLogo());
                defaultSubscription.setDescription(sub.getDescription());
                defaultSubscription.setExpireTime(ts.getExpiredTime());
                return defaultSubscription;
            })
            .toList();
    }

    @Override
    public List<TenantSubscriptionEntity> findSubscriptionsById(String tenantId) {
        val availableSubscriptions = TenantManager.availableSubscriptions();
        val where = Wrappers.lambdaQuery(TenantSubscriptionEntity.class)
            .eq(TenantSubscriptionEntity::getTenantId, tenantId);

        return tenantSubscriptionMapper.selectList(where)
            .stream()
            .filter(ts -> availableSubscriptions.containsKey(ts.getService()))
            .toList();
    }

    @Override
    @Transactional
    public TenantEntity create(@Nonnull TenantDto dto) {
        val username = dto.getUsername();
        val password = dto.getPassword();
        val confirmPassword = dto.getConfirmPassword();

        if (!Strings.CS.equals(password, confirmPassword)) {
            log.error("【创建租户失败】两次输入的密码不一致: {}", dto);
            throw new CommonBizException("两次输入的密码不一致");
        }

        TenantEntity old = this.findByCode(dto.getCode());
        if (old != null) {
            throw new CommonBizException(new DuplicateError(StrUtil.format("代码'{}'已存在", dto.getCode())));
        }

        if (this.phoneExist(dto.getPhone())) {
            throw new CommonBizException(String.format("手机号%s已被占用", dto.getPhone()));
        }

        Long parentId = 0L;
        val tenantId = dto.getTenantId();
        if (!"0".equals(tenantId)) {
            val parent = getById(tenantId);
            if (parent != null) {
                parentId = parent.getId();
                dto.setAgentId(parent.getAgentId());
            }
            else {
                throw new CommonBizException("租户/机构不存在");
            }
        }

        // 创建租户
        TenantEntity tenant = new TenantEntity();
        tenant.setParentId(parentId);
        tenant.setCode(dto.getCode());
        tenant.setName(dto.getName());
        tenant.setShortName(dto.getShortName());
        tenant.setContact(dto.getContact());
        tenant.setAddress(dto.getAddress());
        tenant.setLogo(dto.getLogo());
        tenant.setUid(0L);
        tenant.setAgentId(dto.getAgentId());
        tenant.setStatus(dto.getStatus());
        tenant.setPhone(dto.getPhone());
        tenant.setRemark(dto.getRemark());
        if (!this.save(tenant)) {
            new ResourceCreateError("租户").emit();
        }

        // 创建租户管理员
        UserEntity user = userRepository.findByProviderAndAppIdAndOpenId("phone", "", dto.getPhone());

        if (user == null && StringUtils.isNotBlank(dto.getUsername())) {
            user = userRepository.findByUsername(username);
        }

        if (user == null && StringUtils.isNotBlank(dto.getUsername())) {
            val userDto = new UserDto();
            userDto.setTenantId(String.valueOf(tenant.getId()));
            userDto.setUsername(username);
            userDto.setNickname(StringUtils.defaultIfBlank(dto.getNickname(), dto.getShortName()));
            userDto.setPassword(password);
            userDto.setConfirmPassword(confirmPassword);
            userDto.setPhone(dto.getPhone());
            userDto.setEnableSmsLogin(true);
            userDto.setStatus(UserStatus.ACTIVATED);
            userDto.setResetPassword(true);
            user = userRepository.create(userDto);
            if (user == null) {
                log.error("【创建租户失败】创建租户管理员失败: {}", dto);
                throw new CommonBizException("创建租户管理员失败");
            }
        }
        else if (user == null
                || KalamiContextHolder.getBean(TenantRepository.class).join(tenant.getId(), user.getId()) == null) {
            throw new CommonBizException("创建租户管理员失败");
        }

        // 绑手机号
        userRepository.bindPhone(user.getId(), dto.getPhone());

        // 更新租户超管
        tenant.setUid(user.getId());
        updateById(tenant);

        // 同步模板角色与权限
        if (syncPrivileges(tenant)) {
            // 授权
            val role = roleRepository.findByRoleAndTenantId("tsa", String.valueOf(tenant.getId()));
            if (role == null) {
                log.error("【创建租户失败】角色'租户管理员(tsa)'不存在: {}", dto);
                throw new CommonBizException("角色'租户管理员'不存在");
            }

            userRepository.grantRoles(user.getId(), List.of(role));
        }

        // 订阅基础服务
        val subscribe = new SubscribeDto();
        subscribe.setTenantId(String.valueOf(tenant.getId()));
        subscribe.setService("core");
        subscribe.setExpired(0);
        this.subscribe(subscribe);

        // 发布事件
        val source = tenant.toTenant();
        source.setStatus(TenantStatus.ENABLED.name());

        publisher.publishEvent(new TenantCreatedEvent(source));

        return tenant;
    }

    @Override
    @Transactional
    public TenantEntity update(@Nonnull TenantDto dto) {
        val tenant = getById(dto.getId());
        if (tenant == null) {
            throw new CommonBizException(new ResourceNotFoundError("租户", String.valueOf(dto.getId())));
        }
        val oldPhone = tenant.getPhone();

        BeanUtil.copyProperties(dto, tenant, CopyOptions.create().setIgnoreNullValue(true).setIgnoreProperties("code"));

        if (!oldPhone.equals(dto.getPhone()) && this.phoneExist(dto.getPhone())) {
            throw new CommonBizException(String.format("手机号%s已被占用", dto.getPhone()));
        }

        if (this.updateById(tenant)) {
            if (!oldPhone.equals(dto.getPhone()) && StringUtils.isNotBlank(dto.getPhone())) {
                userRepository.bindPhone(tenant.getUid(), dto.getPhone());
            }

            val source = tenant.toTenant();
            source.setStatus(TenantStatus.ENABLED.name());

            publisher.publishEvent(new TenantModifiedEvent(source));
            return tenant;
        }

        log.error("更新租户失败[DB]： {} ", dto);

        throw new CommonBizException("更新租户失败");
    }

    @Override
    @CacheEvict(cacheNames = "uc:tn:subscription", key = "#dto.tenantId")
    @Transactional
    public DefaultSubscription subscribe(@Nonnull SubscribeDto dto) {
        val serviceId = dto.getService();
        if (!properties.getService().containsKey(serviceId)) {
            throw new CommonBizException(String.format("【%s】不存在", serviceId));
        }
        val service = properties.getService().get(serviceId);
        val tenant = getById(dto.getTenantId());

        if (tenant == null) {
            throw new CommonBizException(String.format("租户【%s】不存在", dto.getTenantId()));
        }

        TenantSubscriptionEntity subscription = tenantSubscriptionMapper.getByTenantAndService(dto.getTenantId(),
                serviceId);

        if (subscription == null) {
            subscription = new TenantSubscriptionEntity();
            subscription.setTenantId(dto.getTenantId());
            subscription.setService(serviceId);
        }

        if (dto.getExpireTime() != null) {
            subscription.setExpiredTime(dto.getExpireTime());
            subscription.setExpired(dto.getExpireTime().isBefore(LocalDateTime.now()) ? 1 : 0);
        }
        else if (dto.getExpired() != null) {
            subscription.setExpired(dto.getExpired());
        }

        if (subscription.getId() == null) {
            if (tenantSubscriptionMapper.insert(subscription) > 0) {
                val ds = new DefaultSubscription();
                ds.setId(serviceId);
                ds.setName(service.getName());
                ds.setLogo(service.getLogo());
                ds.setDescription(service.getDescription());
                ds.setExpireTime(subscription.getExpiredTime());
                ds.setExpired(subscription.getExpired());
                return ds;
            }
        }
        else {
            if (Boolean.TRUE.equals(subscription.getDeleted())) {
                tenantSubscriptionMapper.subscribe(subscription.getTenantId(), serviceId);
            }

            val wrapper = Wrappers.lambdaUpdate(TenantSubscriptionEntity.class);
            wrapper.eq(TenantSubscriptionEntity::getTenantId, subscription.getTenantId());
            wrapper.eq(TenantSubscriptionEntity::getService, serviceId);
            wrapper.set(TenantSubscriptionEntity::getExpiredTime, dto.getExpireTime());

            val entity = new TenantSubscriptionEntity();
            entity.setExpired(subscription.getExpired());

            tenantSubscriptionMapper.update(entity, wrapper);

            val ds = new DefaultSubscription();
            ds.setId(serviceId);
            ds.setName(service.getName());
            ds.setLogo(service.getLogo());
            ds.setDescription(service.getDescription());
            ds.setExpireTime(subscription.getExpiredTime());
            ds.setExpired(subscription.getExpired());
            return ds;
        }

        throw new CommonBizException("租户订阅服务失败[DB]");
    }

    @Override
    @CacheEvict(cacheNames = "uc:tn:subscription", key = "#id")
    @Transactional
    public void unsubscribe(String id, String service) {
        val wrapper = Wrappers.lambdaUpdate(TenantSubscriptionEntity.class);
        wrapper.eq(TenantSubscriptionEntity::getTenantId, id);
        wrapper.eq(TenantSubscriptionEntity::getService, service);

        val entity = new TenantSubscriptionEntity();
        entity.setDeleted(true);

        tenantSubscriptionMapper.update(entity, wrapper);
        tenantSubscriptionMapper.unsubscribe(id, service);
    }

    @Override
    public boolean phoneExist(String phone) {
        return this.lambdaQuery().eq(TenantEntity::getPhone, phone).exists();
    }

    @Override
    public boolean syncPrivileges(@Nonnull TenantEntity tenant) {
        // 模板角色
        List<RoleEntity> roles = roleRepository.listLeasableRoles();
        if (roles.isEmpty()) {
            return true;
        }
        val tenantId = String.valueOf(tenant.getId());

        // 模板角色的权限列表
        Map<String, List<RbacPrivilegeEntity>> privileges = new HashMap<>();
        roles.forEach(role -> {
            List<RbacPrivilegeEntity> granted = roleRepository.listPrivilegesById(role.getId(), role.getTenantId());
            if (!CollectionUtils.isEmpty(granted)) {
                privileges.put(role.getRole(), granted);
            }
            val r = roleRepository.findByRoleAndTenantId(role.getRole(), tenantId);
            if (r == null) {
                role.setId(null);
            }
            else {
                role.setId(r.getId());
            }
            role.setTenantId(tenantId);
            role.setBuiltin(true);
        });
        // 同步角色
        val newRoles = roles.stream().filter(role -> role.getId() == null).toList();
        if (!CollectionUtils.isEmpty(newRoles)) {
            if (!roleRepository.saveBatch(newRoles, 100)) {
                throw new CommonBizException("无法同步租户角色");
            }
        }
        // 同步权限
        Set<RbacPrivilegeEntity> newPrivileges = new HashSet<>();
        for (val p : privileges.entrySet()) {
            val ps = p.getValue();
            for (val privilege : ps) {
                privilege.setTenantId(tenantId);
                val entity = privilegeRepository.findByPermissionAndTenantId(privilege.getPermission(), tenantId);
                if (entity != null) {
                    privilege.setId(entity.getId());
                    continue;
                }
                privilege.setBuiltin(true);
                privilege.setId(null);
                newPrivileges.add(privilege);
            }
        }
        if (!newPrivileges.isEmpty()) {
            privilegeRepository.saveBatch(newPrivileges.stream().toList(), 100);
        }
        // 授权
        Set<RolePrivilegeEntity> newBindings = new HashSet<>();
        for (val p : privileges.entrySet()) {
            val role = p.getKey();
            val ps = p.getValue();
            if (ps.isEmpty()) {
                continue;
            }
            val r = roleRepository.findByRoleAndTenantId(role, tenantId);
            if (r == null) {
                continue;
            }

            Set<RbacPrivilegeEntity> pSet = new HashSet<>(roleRepository.listPrivilegesById(r.getId(), tenantId));

            for (RbacPrivilegeEntity privilege : ps) {
                if (pSet.contains(privilege)) {
                    continue;
                }
                val rp = new RolePrivilegeEntity();
                rp.setTenantId(tenantId);
                rp.setRoleId(r.getId());
                rp.setPrivilegeId(privilege.getId());
                newBindings.add(rp);
            }
        }

        if (!newBindings.isEmpty()) {
            roleRepository.grants(newBindings.stream().toList());
        }

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "uc:tn:users", key = "#uid+''")
    public TenantUserEntity join(Long id, Long uid) {
        TenantUserEntity user = this.findUserByTenantAndUidIgnoreDeleted(id, uid);
        if (user == null) {
            user = new TenantUserEntity();
            user.setTenantId(String.valueOf(id));
            user.setUid(uid);
            if (tenantUserMapper.insert(user) > 0) {
                return user;
            }

        }
        else {
            if (recover(id, uid) > 0) {
                user.setDeleted(false);
                return user;
            }
        }

        throw new CommonBizException("无法加入租户");
    }

    @Override
    @CacheEvict(cacheNames = "uc:tn:users", key = "#uid+''")
    public boolean leave(Long id, Long uid) {
        tenantUserMapper.delete(Wrappers.lambdaQuery(TenantUserEntity.class)
            .eq(TenantUserEntity::getUid, uid)
            .eq(TenantUserEntity::getTenantId, id));

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "uc:tn:users", key = "#uid+''")
    public boolean switchTo(Long id, Long uid) {
        val tenant = getById(id);
        val update1 = Wrappers.lambdaUpdate(TenantUserEntity.class)
            .eq(TenantUserEntity::getUid, uid)
            .ne(TenantUserEntity::getTenantId, id)
            .set(TenantUserEntity::getCurrent, false);
        tenantUserMapper.update(update1);

        val update2 = Wrappers.lambdaUpdate(TenantUserEntity.class)
            .eq(TenantUserEntity::getUid, uid)
            .eq(TenantUserEntity::getTenantId, id)
            .set(TenantUserEntity::getCurrent, true);

        tenantUserMapper.update(update2);

        // 发布租户切换成功通知
        val source = tenant.toTenant();
        source.setCurrent(true);

        publisher.publishEvent(new TenantSwitchedEvent(source));
        return true;
    }

    @Override
    public List<Tenant> listChildren(String parentId) {
        if (StringUtils.isBlank(parentId) || "0".equals(parentId)) {
            return Collections.emptyList();
        }

        return lambdaQuery().eq(TenantEntity::getParentId, parentId).list().stream().map(tenant -> {
            val id = String.valueOf(tenant.getId());
            val t = tenant.toTenant();
            t.setChildren(listChildren(id));

            return t;
        }).toList();
    }

    @Override
    public TenantUserEntity findUserByTenantAndUid(Long tenantId, Long uid) {
        val con = Wrappers.lambdaQuery(TenantUserEntity.class)
            .eq(TenantUserEntity::getUid, uid)
            .eq(TenantUserEntity::getTenantId, tenantId);

        return tenantUserMapper.selectOne(con, true);
    }

    private TenantUserEntity findUserByTenantAndUidIgnoreDeleted(Long tenantId, Long uid) {
        return this.baseMapper.findUserByTenantAndUidIgnoreDeleted(tenantId, uid);
    }

    private int recover(Long tenantId, Long uid) {
        return this.baseMapper.recover(tenantId, uid);
    }

}

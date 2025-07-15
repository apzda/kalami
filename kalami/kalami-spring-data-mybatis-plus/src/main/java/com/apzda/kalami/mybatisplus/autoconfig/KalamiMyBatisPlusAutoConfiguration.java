/*
 * Copyright 2023-2025 the original author or authors.
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
package com.apzda.kalami.mybatisplus.autoconfig;

import cn.hutool.core.date.DateUtil;
import com.apzda.kalami.http.ExceptionTransformer;
import com.apzda.kalami.mybatisplus.MybatisPlusConfigureCustomizer;
import com.apzda.kalami.mybatisplus.config.KalamiMybatisPlusConfigProperties;
import com.apzda.kalami.mybatisplus.exception.MybatisExceptionTransformer;
import com.apzda.kalami.mybatisplus.plugin.OptimisticLockerInnerInterceptor;
import com.apzda.kalami.tenant.TenantManager;
import com.apzda.kalami.user.CurrentUserProvider;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.handlers.StrictFill;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@Slf4j
@AutoConfiguration(before = MybatisPlusAutoConfiguration.class)
@ConditionalOnClass(MybatisPlusAutoConfiguration.class)
@EnableConfigurationProperties(KalamiMybatisPlusConfigProperties.class)
public class KalamiMyBatisPlusAutoConfiguration {

    private static final Pattern PATTERN = Pattern.compile("_([a-z])");

    @Bean
    ExceptionTransformer mybatisPlusExceptionTransformer() {
        return new MybatisExceptionTransformer();
    }

    // 增加的mybatis-plus配置
    @Bean
    ConfigurationCustomizer apzdaMybatisPlusConfigurationCustomizer(final ObjectProvider<TenantManager> tenantManager,
            final ObjectProvider<MybatisPlusConfigureCustomizer> customizers, ObjectMapper objectMapper) {
        JacksonTypeHandler.setObjectMapper(objectMapper);

        val ignoreTables = new HashSet<String>();

        customizers.orderedStream().forEach(customizer -> {
            customizer.addTenantIgnoreTable(ignoreTables);
        });

        return configuration -> {
            // 配置分页插件与乐观锁插件
            configuration.getInterceptors()
                .stream()
                .filter(interceptor -> interceptor instanceof MybatisPlusInterceptor)
                .findAny()
                .ifPresentOrElse(interceptor -> {
                    val mybatisPlusInterceptor = (MybatisPlusInterceptor) interceptor;
                    tenantManager.ifAvailable(tm -> {
                        if (tm.disableTenantPlugin()) {
                            return;
                        }
                        val tenantIdColumn = org.apache.commons.lang3.StringUtils.defaultIfBlank(tm.getTenantIdColumn(),
                                "tenant_id");
                        mybatisPlusInterceptor.addInnerInterceptor(new TenantLineInnerInterceptor(
                                new DefaultTenantLineHandler(tenantIdColumn, ignoreTables)));
                    });

                    if (mybatisPlusInterceptor.getInterceptors()
                        .stream()
                        .filter(innerInterceptor -> innerInterceptor instanceof PaginationInnerInterceptor)
                        .findAny()
                        .isEmpty()) {
                        // log.debug("添加分页插件");
                        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
                    }

                    if (mybatisPlusInterceptor.getInterceptors()
                        .stream()
                        .filter(innerInterceptor -> innerInterceptor instanceof com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor)
                        .findAny()
                        .isEmpty()) {
                        // log.debug("添加乐观锁插件");
                        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
                    }
                }, () -> {
                    // log.debug("配置分页与乐观锁插件");
                    val mybatisInterceptor = new MybatisPlusInterceptor();
                    tenantManager.ifAvailable(tm -> {
                        if (tm.disableTenantPlugin()) {
                            return;
                        }
                        val tenantIdColumn = org.apache.commons.lang3.StringUtils.defaultIfBlank(tm.getTenantIdColumn(),
                                "tenant_id");
                        mybatisInterceptor.addInnerInterceptor(new TenantLineInnerInterceptor(
                                new DefaultTenantLineHandler(tenantIdColumn, ignoreTables)));
                    });
                    mybatisInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
                    mybatisInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
                    configuration.addInterceptor(mybatisInterceptor);
                });
        };
    }

    @Bean
    MybatisPlusPropertiesCustomizer apzdaMybatisPlusPropertiesCustomizer(
            final ObjectProvider<MybatisPlusConfigureCustomizer> customizers) {
        return properties -> {
            var mapperLocations = Optional.ofNullable(properties.getMapperLocations())
                .orElse(new String[] { "classpath*:/mapper/**/*Mapper.xml" });

            final Set<String> locations = new HashSet<>(List.of(mapperLocations));
            customizers.orderedStream().forEach(customizer -> {
                customizer.addLocation(locations);
            });

            log.debug("Mapper File Locations: {}", locations);
            String[] locs = new String[locations.size()];
            locs = locations.toArray(locs);
            properties.setMapperLocations(locs);

            // typeHandlersPackage
            val typeHandlersPackage = properties.getTypeHandlersPackage();
            final Set<String> packages = new HashSet<>();
            customizers.orderedStream().forEach(customizer -> {
                customizer.addTypeHandlersPackage(packages);
            });

            if (!CollectionUtils.isEmpty(packages)) {
                if (StringUtils.hasText(typeHandlersPackage)) {
                    properties.setTypeHandlersPackage(typeHandlersPackage + ";" + Joiner.on(";").join(packages));
                }
                else {
                    properties.setTypeHandlersPackage(Joiner.on(";").join(packages));
                }
            }
            log.debug("TypeHandlers Packages: {}", properties.getTypeHandlersPackage());
        };
    }

    @Bean
    MetaObjectHandler gsvcMetaObjectHandler(ObjectProvider<TenantManager> tenantManagers,
            KalamiMybatisPlusConfigProperties properties, @Autowired(required = false) Clock clock) {
        val appClock = Objects.requireNonNullElseGet(clock, Clock::systemDefaultZone);

        val builder = new StringBuilder();
        tenantManagers.ifAvailable(tenantManager -> {
            val tenantIdColumn = org.apache.commons.lang3.StringUtils.defaultIfBlank(tenantManager.getTenantIdColumn(),
                    "tenant_id");
            builder.append(PATTERN.matcher(tenantIdColumn).replaceAll(m -> m.group(1).toUpperCase()));
        });

        val tenantIdColumn = org.apache.commons.lang3.StringUtils.defaultIfBlank(builder.toString(), "tenantId");
        val createdAtColumns = properties.getCreatedAtColumns();
        val createdByColumns = properties.getCreatedByColumns();
        val updatedAtColumns = properties.getUpdatedAtColumns();
        val updatedByColumns = properties.getUpdatedByColumns();

        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                val fills = new ArrayList<StrictFill<?, ?>>();
                // 填充create time
                for (String column : createdAtColumns) {
                    if (!metaObject.hasGetter(column) || !metaObject.hasSetter(column)) {
                        continue;
                    }
                    val timeType = metaObject.getGetterType(column);
                    if (timeType != null) {
                        fills.add(getTime(column, timeType));
                    }
                }
                // 填充update time
                for (String column : updatedAtColumns) {
                    if (!metaObject.hasGetter(column) || !metaObject.hasSetter(column)) {
                        continue;
                    }
                    val timeType = metaObject.getGetterType(column);
                    if (timeType != null) {
                        fills.add(getTime(column, timeType));
                    }
                }
                // 填充create user id
                val currentAuditor = Optional.ofNullable(CurrentUserProvider.getCurrentUser().getUid()).orElse("0");
                val auditor = org.apache.commons.lang3.StringUtils.defaultIfBlank(currentAuditor, "0");
                for (String column : createdByColumns) {
                    if (!metaObject.hasGetter(column) || !metaObject.hasSetter(column)) {
                        continue;
                    }
                    val uidType = metaObject.getGetterType(column);
                    if (uidType != null) {
                        fills.add(getUid(column, uidType, auditor));
                    }
                }

                // 填充last_modified_user_id
                for (String column : updatedByColumns) {
                    if (!metaObject.hasGetter(column) || !metaObject.hasSetter(column)) {
                        continue;
                    }
                    val uidType = metaObject.getGetterType(column);
                    if (uidType != null) {
                        fills.add(getUid(column, uidType, auditor));
                    }
                }

                // 填充tenantId
                if (metaObject.hasGetter(tenantIdColumn) && metaObject.hasSetter(tenantIdColumn)) {
                    val idType = metaObject.getGetterType(tenantIdColumn);
                    if (idType != null) {
                        val tid = TenantManager.tenantId();
                        String tenantId;
                        if (tid == null) {
                            tenantId = "0";
                        }
                        else {
                            tenantId = String.valueOf(tid);
                        }
                        fills.add(getUid(tenantIdColumn, idType, tenantId));
                    }
                }

                // 填充@Version
                val fieldName = "version";
                if (metaObject.hasGetter(fieldName) && metaObject.hasSetter(fieldName)) {
                    val idType = metaObject.getGetterType(fieldName);
                    if (idType != null) {
                        fills.add(getUid(fieldName, idType, "0"));
                    }
                }

                if (!fills.isEmpty()) {
                    strictInsertFill(findTableInfo(metaObject), metaObject, fills);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                val fills = new ArrayList<StrictFill<?, ?>>();
                for (String column : updatedAtColumns) {
                    if (!metaObject.hasGetter(column) || !metaObject.hasSetter(column)) {
                        continue;
                    }
                    val timeType = metaObject.getGetterType(column);
                    if (timeType != null) {
                        fills.add(getTime(column, timeType));
                    }
                }

                val currentAuditor = Optional.ofNullable(CurrentUserProvider.getCurrentUser().getUid()).orElse("0");
                val auditor = org.apache.commons.lang3.StringUtils.defaultIfBlank(currentAuditor, "0");
                for (String column : updatedByColumns) {
                    if (!metaObject.hasGetter(column) || !metaObject.hasSetter(column)) {
                        continue;
                    }
                    val uidType = metaObject.getGetterType(column);
                    if (uidType != null) {
                        fills.add(getUid(column, uidType, auditor));
                    }
                }

                if (!fills.isEmpty()) {
                    strictUpdateFill(findTableInfo(metaObject), metaObject, fills);
                }
            }

            @Nonnull
            private ClonableStrictFill<?, ?> getUid(String name, Class<?> userIdClz, String userId) {
                ClonableStrictFill<?, ?> uid;
                if (userIdClz == null) {
                    uid = new ClonableStrictFill<>(name, String.class, () -> null);
                }
                else if (Short.class.isAssignableFrom(userIdClz)) {
                    uid = new ClonableStrictFill<>(name, Short.class, () -> Short.parseShort(userId));
                }
                else if (Long.class.isAssignableFrom(userIdClz)) {
                    uid = new ClonableStrictFill<>(name, Long.class, () -> Long.parseLong(userId));
                }
                else if (Integer.class.isAssignableFrom(userIdClz)) {
                    uid = new ClonableStrictFill<>(name, Integer.class, () -> Integer.parseInt(userId));
                }
                else if (org.apache.commons.lang3.StringUtils.isNotBlank(userId)) {
                    uid = new ClonableStrictFill<>(name, String.class, () -> userId);
                }
                else {
                    uid = new ClonableStrictFill<>(name, String.class, () -> null);
                }
                return uid;
            }

            @Nonnull
            private ClonableStrictFill<?, ?> getTime(String name, Class<?> timeType) {
                ClonableStrictFill<?, ?> current;
                if (timeType == null || Long.class.isAssignableFrom(timeType)) {
                    current = new ClonableStrictFill<>(name, Long.class, appClock::millis);
                }
                else if (Date.class.isAssignableFrom(timeType)) {
                    current = new ClonableStrictFill<>(name, Date.class, Date::new);
                }
                else if (LocalDate.class.isAssignableFrom(timeType)) {
                    current = new ClonableStrictFill<>(name, LocalDate.class, () -> LocalDate.now(appClock));
                }
                else if (LocalDateTime.class.isAssignableFrom(timeType)) {
                    current = new ClonableStrictFill<>(name, LocalDateTime.class, () -> LocalDateTime.now(appClock));
                }
                else {
                    current = new ClonableStrictFill<>(name, String.class,
                            () -> DateUtil.formatLocalDateTime(LocalDateTime.now(appClock)));
                }
                return current;
            }
        };
    }

    private record DefaultTenantLineHandler(String tenantIdColumn,
            Set<String> ignoreTables) implements TenantLineHandler {
        @Override
        @Nonnull
        public Expression getTenantId() {

            String tenantId = TenantManager.tenantId();

            if (org.apache.commons.lang3.StringUtils.isBlank(tenantId)) {
                return new NullValue();
            }

            return new StringValue(tenantId);
        }

        @Override
        public boolean ignoreTable(String tableName) {
            return ignoreTables.contains(tableName);
        }

        @Override
        public String getTenantIdColumn() {
            return tenantIdColumn;
        }

    }

    private static class ClonableStrictFill<T, E extends T> extends StrictFill<T, E> {

        public ClonableStrictFill(String fieldName, Class<T> fieldType, Supplier<E> fieldVal) {
            super(fieldName, fieldType, fieldVal);
        }

        @Nonnull
        public ClonableStrictFill<T, E> changeFieldName(String name) {
            return new ClonableStrictFill<>(name, this.getFieldType(), this.getFieldVal());
        }

    }

}

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
package com.apzda.kalami.mybatisplus;

import com.apzda.kalami.mybatisplus.autoconfig.KalamiMyBatisPlusAutoConfiguration;
import com.apzda.kalami.mybatisplus.config.KalamiMybatisPlusConfigProperties;
import com.apzda.kalami.mybatisplus.entity.Role;
import com.apzda.kalami.mybatisplus.entity.User;
import com.apzda.kalami.mybatisplus.mapper.RoleMapper;
import com.apzda.kalami.mybatisplus.mapper.UserMapper;
import com.apzda.kalami.mybatisplus.service.impl.UserService;
import com.apzda.kalami.tenant.TenantManager;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import jakarta.annotation.Resource;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.Clock;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@MybatisPlusTest
@ContextConfiguration(classes = TestApp.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration({ KalamiMyBatisPlusAutoConfiguration.class })
@Sql(value = "classpath:/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "classpath:/tear.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@TestPropertySource(properties = { "kalami.mybatis-plus.disable-tenant-plugin=false",
        "kalami.mybatis-plus.tenant-id-column=merchant_id" })
@EnableConfigurationProperties(KalamiMybatisPlusConfigProperties.class)
class MybatisPlusAutoConfigureTest {

    @MockitoBean
    private Clock clock;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private TenantManager tenantManager;

    @Resource(type = UserMapper.class)
    private UserMapper userMapper;

    @Resource(type = UserService.class)
    private UserService userService;

    @Resource(type = RoleMapper.class)
    private RoleMapper roleMapper;

    @BeforeEach
    void create() {
        when(clock.millis()).thenReturn(System.currentTimeMillis());
        // when(serviceConfigProperties.getMybatisPlus()).thenAnswer(invocation ->
        // p.getMybatisPlus());

        {
            var user = new User();
            user.setName("1");
            user.setUid("1");
            user.setRoles(Arrays.asList("1", "2", "3"));
            userService.save(user);

            user = new User();
            user.setName("2");
            user.setUid("2");
            userService.save(user);

            user = new User();
            user.setName("3");
            user.setUid("3");
            userService.save(user);

            user = new User();
            user.setName("4");
            user.setUid("4");
            userService.save(user);

            user = new User();
            user.setName("5");
            user.setUid("5");
            userService.save(user);

            user = new User();
            user.setName("6");
            userService.save(user);

            user = new User();
            user.setName("7");
            user.setMerchantId("987654321");
            userService.save(user);
        }

        {
            var role = new Role();
            role.setRid("1");
            role.setName("1");
            role.setDd(0.25d);
            roleMapper.insert(role);

            role = new Role();
            role.setRid("2");
            role.setName("2");
            role.setDd(0.55d);
            roleMapper.insert(role);
        }
    }

    @Test
    void testUser() {
        assertThat(tenantManager.getTenantIdColumn()).isEqualTo("merchant_id");
        assertThat(tenantManager.disableTenantPlugin()).isFalse();
        assertThat(context).isNotNull();
        val user1 = userMapper.selectById("1");
        assertThat(user1).isNotNull();
        assertThat(user1.getCreatedBy()).isEqualTo("1");
        assertThat(user1.getCreatedAt()).isNotNull();
        assertThat(user1.getMerchantId()).isEqualTo("123456789");
        val user = userMapper.getUserById("1");
        assertThat(user).isNotNull();
        assertThat(user.getVer()).isEqualTo(0);
        assertThat(user.getRoles()).isNotEmpty().contains("1", "2", "3");

        user.setName("22");
        val affectedRows = userMapper.updateById(user);
        assertThat(affectedRows).isEqualTo(1);
        assertThat(user.getUpdatedBy()).isEqualTo("1");

        user1.setName("2222");
        assertThat(userMapper.updateById(user1)).isEqualTo(0);

        IPage<User> page = new Page<>();
        page.setSize(2);
        val con = Wrappers.lambdaQuery(User.class);
        con.eq(User::getMerchantId, "123456789");
        userMapper.selectPage(page, con);
        assertThat(page).isNotNull();
        assertThat(page.getTotal()).isEqualTo(6);
        assertThat(page.getPages()).isEqualTo(3);

        val user6 = userMapper.getUserByName("6");
        assertThat(user6.getUid().length()).isGreaterThan(9);

        val user7 = userMapper.getUserByName("7");
        if (tenantManager.disableTenantPlugin()) {
            assertThat(user7.getMerchantId()).isEqualTo("987654321");
        }
        else {
            assertThat(user7).isNull();
        }
    }

    @Test
    void testRole() {
        var role = roleMapper.selectById("1");
        assertThat(role).isNotNull();

        role = roleMapper.getRoleById("1");
        assertThat(role).isNotNull();
        assertThat(role.getDd()).isEqualTo(0.25d);
    }

}

/*
 * Copyright 2023-2025 Fengz Ning (windywany@gmail.com)
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
package com.apzda.kalami.mybatisplus.plugin;

import com.apzda.kalami.mybatisplus.TestApp;
import com.apzda.kalami.mybatisplus.autoconfig.MyBatisPlusAutoConfiguration;
import com.apzda.kalami.mybatisplus.config.KalamiMybatisPlusConfigProperties;
import com.apzda.kalami.mybatisplus.mapper.RoleMapper;
import com.apzda.kalami.mybatisplus.service.IRoleService;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@MybatisPlusTest
@ContextConfiguration(classes = TestApp.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration({ MyBatisPlusAutoConfiguration.class })
@ComponentScan("com.apzda.cloud.mybatis.service")
@Sql(value = "classpath:/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "classpath:/tear.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@EnableConfigurationProperties(KalamiMybatisPlusConfigProperties.class)
class OptimisticLockerInnerInterceptorTest {

    @MockitoBean
    private Clock clock;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private IRoleService roleService;

    @Test
    @Sql("classpath:/dml.sql")
    void optimisticLockerInnerInterceptorShouldWork() {
        // given
        val role = roleMapper.selectById(1);
        val role2 = roleMapper.selectById(1);
        val role3 = roleMapper.selectById(1);
        // when
        role.setName("t1");
        roleMapper.updateById(role);

        role2.setName("t2");
        val rst = roleMapper.updateById(role2);
        assertThat(rst).isEqualTo(0);

        role3.setName("t3");
        // Exception?
        assertThat(roleService.updateById(role3)).isFalse();
    }

}

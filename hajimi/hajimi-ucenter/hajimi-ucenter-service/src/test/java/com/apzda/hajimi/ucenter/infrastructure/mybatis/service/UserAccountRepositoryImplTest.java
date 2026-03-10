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
package com.apzda.hajimi.ucenter.infrastructure.mybatis.service;

import cn.hutool.core.bean.BeanUtil;
import com.apzda.hajimi.ucenter.app.TestApp;
import com.apzda.hajimi.ucenter.config.UCenterConfigProperties;
import com.apzda.hajimi.ucenter.domain.entity.UserAccountEntity;
import com.apzda.hajimi.ucenter.domain.enums.Asset;
import com.apzda.hajimi.ucenter.domain.enums.TransStatus;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountRefundRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountTransactionRepository;
import com.apzda.hajimi.ucenter.domain.repository.UserAccountTransitionRepository;
import com.apzda.hajimi.ucenter.security.token.UCenterTokenManager;
import com.apzda.hajimi.ucenter.service.dto.RefundDto;
import com.apzda.hajimi.ucenter.service.dto.TradeDto;
import com.apzda.kalami.exception.CommonBizException;
import com.apzda.kalami.mybatisplus.autoconfig.KalamiMyBatisPlusAutoConfiguration;
import com.apzda.kalami.service.CounterService;
import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.util.DigestUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author ninggf (windywany@gmail.com)
 * @version 1.0.0
 */
@MybatisPlusTest
@ContextConfiguration(classes = TestApp.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureJson
@AutoConfigureDataRedis
@ImportAutoConfiguration({ KalamiMyBatisPlusAutoConfiguration.class })
@ComponentScan({ "com.apzda.hajimi.ucenter.infrastructure" })
@MapperScan("com.apzda.hajimi.ucenter.infrastructure.mybatis.mapper")
@Testcontainers(parallel = true)
@Sql(value = { "file:../schema/mysql/1.0.0.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserAccountRepositoryImplTest {

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UCenterTokenManager ucenterTokenManager;

    @MockitoBean
    private UCenterConfigProperties ucenterConfigProperties;

    @MockitoBean
    private CounterService counterService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserAccountRefundRepository refundRepository;

    @Autowired
    private UserAccountTransitionRepository transitionRepository;

    @Autowired
    private UserAccountTransactionRepository transactionRepository;

    @Test
    @DisplayName("获取用户账户")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/trade1.sql")
    void getAccount() {
        // when
        val account = userAccountRepository.getByTenantIdAndUidAndAsset("0", 100L, Asset.CASH);
        assertThat(account).isNotNull();
    }

    @Test
    @DisplayName("更新账户")
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/trade1.sql")
    void updateAccount() {
        // when
        val account = userAccountRepository.getByTenantIdAndUidAndAsset("0", 100L, Asset.CASH);
        assertThat(account).isNotNull();

        val account2 = BeanUtil.copyProperties(account, UserAccountEntity.class);

        account.setBalance(new BigDecimal("100"));
        userAccountRepository.updateAccount(account);

        account2.setBalance(new BigDecimal("200"));
        assertThatThrownBy(() -> userAccountRepository.updateAccount(account2)).isInstanceOf(CommonBizException.class);
    }

    @Test
    @DisplayName("加值交易")
    void trade() {
        // given
        TradeDto tradeDto = new TradeDto();
        tradeDto.setAdvance(false);
        tradeDto.setUid(1L);
        tradeDto.setAsset(Asset.CASH);
        tradeDto.setAmount(new BigDecimal("1"));
        tradeDto.setBizType("test");
        tradeDto.setBizSubject("test");
        tradeDto.setBizOrderNo("T0001");
        tradeDto.setRemark("测试");
        // when
        val trans = userAccountRepository.trade(tradeDto);

        // then
        assertThat(trans).isNotNull();
        assertThat(trans.getPreBalance()).isEqualTo(new BigDecimal("0"));
        assertThat(trans.getPreFrozen()).isEqualTo(new BigDecimal("0"));
        assertThat(trans.getAmount()).isEqualTo(new BigDecimal("1"));
        assertThat(trans.getBalance()).isEqualTo(new BigDecimal("1"));
        assertThat(trans.getFrozen()).isEqualTo(new BigDecimal("0"));
        assertThat(trans.getTransHash()).isEqualTo(DigestUtils.md5DigestAsHex("01CASHtestT0001".getBytes()));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/trade1.sql")
    @DisplayName("减值交易")
    void deduct() {
        // given
        TradeDto tradeDto = new TradeDto();
        tradeDto.setAdvance(false);
        tradeDto.setUid(1L);
        tradeDto.setAsset(Asset.CASH);
        tradeDto.setAmount(new BigDecimal("-90.000"));
        tradeDto.setBizType("test");
        tradeDto.setBizSubject("test");
        tradeDto.setBizOrderNo("W0001");
        tradeDto.setRemark("测试");
        // when
        val trans = userAccountRepository.trade(tradeDto);

        // then
        assertThat(trans).isNotNull();
        assertThat(trans.getPreBalance()).isEqualTo(new BigDecimal("100.000"));
        assertThat(trans.getPreFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(trans.getAmount()).isEqualTo(new BigDecimal("-90.000"));
        assertThat(trans.getBalance()).isEqualTo(new BigDecimal("10.000"));
        assertThat(trans.getFrozen()).isEqualTo(new BigDecimal("0.000"));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/trade1.sql")
    @DisplayName("余额不足交易")
    void deductWhileNotEnoughMoney() {
        // given
        TradeDto tradeDto = new TradeDto();
        tradeDto.setAdvance(false);
        tradeDto.setUid(1L);
        tradeDto.setAsset(Asset.CASH);
        tradeDto.setAmount(new BigDecimal("-190.000"));
        tradeDto.setBizType("test");
        tradeDto.setBizSubject("test");
        tradeDto.setBizOrderNo("W0002");
        tradeDto.setRemark("测试");
        // when
        assertThatThrownBy(() -> userAccountRepository.trade(tradeDto)).isInstanceOf(CommonBizException.class);
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/trade1.sql")
    @DisplayName("预扣款并确认")
    void advanceDeduct() {
        // given
        TradeDto tradeDto = new TradeDto();
        tradeDto.setAdvance(true);
        tradeDto.setUid(1L);
        tradeDto.setAsset(Asset.CASH);
        tradeDto.setAmount(new BigDecimal("-90.000"));
        tradeDto.setBizType("test");
        tradeDto.setBizSubject("test");
        tradeDto.setBizOrderNo("A0001");
        tradeDto.setRemark("测试");
        // when
        val trans = userAccountRepository.trade(tradeDto);
        val account = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val transit = transitionRepository.getById("0", trans.getId());
        // then
        assertThat(trans).isNotNull();
        assertThat(trans.getStatus()).isEqualTo(TransStatus.PENDING);

        assertThat(transit).isNotNull();
        assertThat(transit.getAmount()).isEqualTo(new BigDecimal("-90.000"));
        assertThat(transit.getStatus()).isEqualTo(TransStatus.PENDING);

        assertThat(account.getBalance()).isEqualTo(new BigDecimal("10.000"));
        assertThat(account.getFrozen()).isEqualTo(new BigDecimal("90.000"));
        assertThat(account.getTransition()).isEqualTo(new BigDecimal("0.000"));

        // 确认
        val confirm = userAccountRepository.confirm("0", trans.getId());
        assertThat(confirm).isNotNull();

        val account1 = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val transit1 = transitionRepository.getById("0", trans.getId());
        val cTrans = transactionRepository.getByTransitId("0", transit1.getId());

        assertThat(transit1).isNotNull();
        assertThat(transit1.getStatus()).isEqualTo(TransStatus.DONE);

        assertThat(cTrans).isNotNull();
        assertThat(cTrans.getPreFrozen()).isEqualTo(new BigDecimal("90.000"));
        assertThat(cTrans.getPreBalance()).isEqualTo(new BigDecimal("10.000"));
        assertThat(cTrans.getAmount()).isEqualTo(new BigDecimal("-90.000"));
        assertThat(cTrans.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(cTrans.getBalance()).isEqualTo(new BigDecimal("10.000"));

        assertThat(account1.getBalance()).isEqualTo(new BigDecimal("10.000"));
        assertThat(account1.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(account1.getTransition()).isEqualTo(new BigDecimal("0.000"));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/trade1.sql")
    @DisplayName("预扣款并取消")
    void cancelDeduct() {
        // given
        TradeDto tradeDto = new TradeDto();
        tradeDto.setAdvance(true);
        tradeDto.setUid(1L);
        tradeDto.setAsset(Asset.CASH);
        tradeDto.setAmount(new BigDecimal("-90.000"));
        tradeDto.setBizType("test");
        tradeDto.setBizSubject("test");
        tradeDto.setBizOrderNo("A0001");
        tradeDto.setRemark("测试");
        // when
        val trans = userAccountRepository.trade(tradeDto);
        val account = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val transit = transitionRepository.getById("0", trans.getId());
        // then
        assertThat(trans).isNotNull();
        assertThat(trans.getStatus()).isEqualTo(TransStatus.PENDING);

        assertThat(transit).isNotNull();
        assertThat(transit.getAmount()).isEqualTo(new BigDecimal("-90.000"));
        assertThat(transit.getStatus()).isEqualTo(TransStatus.PENDING);

        assertThat(account.getBalance()).isEqualTo(new BigDecimal("10.000"));
        assertThat(account.getFrozen()).isEqualTo(new BigDecimal("90.000"));
        assertThat(account.getTransition()).isEqualTo(new BigDecimal("0.000"));

        // 取消
        val confirm = userAccountRepository.cancel("0", trans.getId(), "测试");
        assertThat(confirm).isNotNull();

        val account1 = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val transit1 = transitionRepository.getById("0", trans.getId());

        assertThat(transit1).isNotNull();
        assertThat(transit1.getStatus()).isEqualTo(TransStatus.CANCELED);

        assertThat(account1.getBalance()).isEqualTo(new BigDecimal("100.000"));
        assertThat(account1.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(account1.getTransition()).isEqualTo(new BigDecimal("0.000"));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/trade1.sql")
    @DisplayName("预收入并确认")
    void advanceDeposit() {
        // given
        TradeDto tradeDto = new TradeDto();
        tradeDto.setAdvance(true);
        tradeDto.setUid(1L);
        tradeDto.setAsset(Asset.CASH);
        tradeDto.setAmount(new BigDecimal("90.000"));
        tradeDto.setBizType("test");
        tradeDto.setBizSubject("test");
        tradeDto.setBizOrderNo("B0001");
        tradeDto.setRemark("测试");
        // when
        val trans = userAccountRepository.trade(tradeDto);
        val account = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val transit = transitionRepository.getById("0", trans.getId());
        // then
        assertThat(trans).isNotNull();
        assertThat(trans.getStatus()).isEqualTo(TransStatus.PENDING);

        assertThat(transit).isNotNull();
        assertThat(transit.getAmount()).isEqualTo(new BigDecimal("90.000"));
        assertThat(transit.getStatus()).isEqualTo(TransStatus.PENDING);

        assertThat(account.getBalance()).isEqualTo(new BigDecimal("100.000"));
        assertThat(account.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(account.getTransition()).isEqualTo(new BigDecimal("90.000"));

        // 确认
        val confirm = userAccountRepository.confirm("0", trans.getId());
        assertThat(confirm).isNotNull();

        val account1 = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val transit1 = transitionRepository.getById("0", trans.getId());
        val cTrans = transactionRepository.getByTransitId("0", transit1.getId());

        assertThat(transit1).isNotNull();
        assertThat(transit1.getStatus()).isEqualTo(TransStatus.DONE);

        assertThat(cTrans).isNotNull();
        assertThat(cTrans.getPreFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(cTrans.getPreBalance()).isEqualTo(new BigDecimal("100.000"));
        assertThat(cTrans.getAmount()).isEqualTo(new BigDecimal("90.000"));
        assertThat(cTrans.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(cTrans.getBalance()).isEqualTo(new BigDecimal("190.000"));

        assertThat(account1.getBalance()).isEqualTo(new BigDecimal("190.000"));
        assertThat(account1.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(account1.getTransition()).isEqualTo(new BigDecimal("0.000"));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/trade1.sql")
    @DisplayName("预收入并取消")
    void cancelDeposit() {
        // given
        TradeDto tradeDto = new TradeDto();
        tradeDto.setAdvance(true);
        tradeDto.setUid(1L);
        tradeDto.setAsset(Asset.CASH);
        tradeDto.setAmount(new BigDecimal("90.000"));
        tradeDto.setBizType("test");
        tradeDto.setBizSubject("test");
        tradeDto.setBizOrderNo("B0001");
        tradeDto.setRemark("测试");
        // when
        val trans = userAccountRepository.trade(tradeDto);
        val account = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val transit = transitionRepository.getById("0", trans.getId());
        // then
        assertThat(trans).isNotNull();
        assertThat(trans.getStatus()).isEqualTo(TransStatus.PENDING);

        assertThat(transit).isNotNull();
        assertThat(transit.getAmount()).isEqualTo(new BigDecimal("90.000"));
        assertThat(transit.getStatus()).isEqualTo(TransStatus.PENDING);

        assertThat(account.getBalance()).isEqualTo(new BigDecimal("100.000"));
        assertThat(account.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(account.getTransition()).isEqualTo(new BigDecimal("90.000"));

        // 确认
        val confirm = userAccountRepository.cancel("0", trans.getId(), "测试一哈");
        assertThat(confirm).isNotNull();

        val account1 = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val transit1 = transitionRepository.getById("0", trans.getId());

        assertThat(transit1).isNotNull();
        assertThat(transit1.getStatus()).isEqualTo(TransStatus.CANCELED);
        assertThat(transit1.getCancelReason()).isEqualTo("测试一哈");

        assertThat(account1.getBalance()).isEqualTo(new BigDecimal("100.000"));
        assertThat(account1.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(account1.getTransition()).isEqualTo(new BigDecimal("0.000"));
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:/trade1.sql")
    @DisplayName("退款")
    void refund() {
        // given
        RefundDto refundDto = new RefundDto();
        refundDto.setUid(1L);
        refundDto.setAsset(Asset.CASH);
        refundDto.setAmount(new BigDecimal("90.000"));
        refundDto.setTransId(1970733012083965954L);
        refundDto.setBizType("test");
        refundDto.setSubject("refund");
        refundDto.setOrderNo("R0001");
        refundDto.setReason("测试退款");

        // when
        val refund = userAccountRepository.refund(refundDto);
        val refunded = refundRepository.refundedAmount("0", 1970733012083965954L);
        val account = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val trans = transactionRepository.getByTransactionHash("0",
                DigestUtils.md5DigestAsHex(("01CASHtest" + refund.getId()).getBytes()));
        // then
        assertThat(trans.getPreBalance()).isEqualTo(new BigDecimal("100.000"));
        assertThat(trans.getPreFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(trans.getAmount()).isEqualTo(new BigDecimal("-90.000"));
        assertThat(trans.getBalance()).isEqualTo(new BigDecimal("10.000"));
        assertThat(trans.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(refunded).isEqualTo(new BigDecimal("90.000"));

        assertThat(account).isNotNull();
        assertThat(account.getBalance()).isEqualTo(new BigDecimal("10.000"));

        // given
        refundDto.setAmount(new BigDecimal("5.000"));
        refundDto.setOrderNo("R0002");
        val refund1 = userAccountRepository.refund(refundDto);
        val refunded1 = refundRepository.refundedAmount("0", 1970733012083965954L);
        val account1 = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val trans1 = transactionRepository.getByTransactionHash("0",
                DigestUtils.md5DigestAsHex(("01CASHtest" + refund1.getId()).getBytes()));
        // then
        assertThat(trans1.getPreBalance()).isEqualTo(new BigDecimal("10.000"));
        assertThat(trans1.getPreFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(trans1.getAmount()).isEqualTo(new BigDecimal("-5.000"));
        assertThat(trans1.getBalance()).isEqualTo(new BigDecimal("5.000"));
        assertThat(trans1.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(refunded1).isEqualTo(new BigDecimal("95.000"));

        assertThat(account1).isNotNull();
        assertThat(account1.getBalance()).isEqualTo(new BigDecimal("5.000"));

        // given
        refundDto.setAmount(new BigDecimal("-15.000"));
        refundDto.setOrderNo("R0003");
        val refund2 = userAccountRepository.refund(refundDto);
        val refunded2 = refundRepository.refundedAmount("0", 1970733012083965954L);
        val account2 = userAccountRepository.getByTenantIdAndUidAndAsset("0", 1L, Asset.CASH);
        val trans2 = transactionRepository.getByTransactionHash("0",
                DigestUtils.md5DigestAsHex(("01CASHtest" + refund2.getId()).getBytes()));
        // then
        assertThat(trans2.getPreBalance()).isEqualTo(new BigDecimal("5.000"));
        assertThat(trans2.getPreFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(trans2.getAmount()).isEqualTo(new BigDecimal("15.000"));
        assertThat(trans2.getBalance()).isEqualTo(new BigDecimal("20.000"));
        assertThat(trans2.getFrozen()).isEqualTo(new BigDecimal("0.000"));
        assertThat(refunded2).isEqualTo(new BigDecimal("80.000"));

        assertThat(account2).isNotNull();
        assertThat(account2.getBalance()).isEqualTo(new BigDecimal("20.000"));

        refundDto.setAmount(new BigDecimal("25.000"));
        refundDto.setOrderNo("R0004");
        assertThatThrownBy(() -> userAccountRepository.refund(refundDto)).isInstanceOf(CommonBizException.class);
    }

}

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
package com.apzda.kalami.data.jpa;

import com.apzda.kalami.data.domain.TenantAware;
import com.apzda.kalami.data.jpa.entity.AuditableEntity;
import com.apzda.kalami.data.jpa.entity.TestEntity;
import com.apzda.kalami.data.jpa.repository.TestEntityRepository;
import com.apzda.kalami.user.CurrentUser;
import com.apzda.kalami.user.CurrentUserProvider;
import com.apzda.kalami.user.TenantManager;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ninggf (windywany@gmail.com)
 * @since 2025/05/16
 * @version 1.0.0
 */
@DataJpaTest
@ContextConfiguration(classes = AuditingEntityListenerTest.class)
@EntityScan(basePackageClasses = AuditingEntityListenerTest.class)
@EnableJpaRepositories(basePackageClasses = AuditingEntityListenerTest.class)
class AuditingEntityListenerTest {

    @Autowired
    private TestEntityRepository testEntityRepository;

    @Test
    void fillMetaData() {
        // given
        val listener = new AuditingEntityListener();
        val entity = new TestEntity();
        try (val ms = Mockito.mockStatic(CurrentUserProvider.class); val ts = Mockito.mockStatic(TenantManager.class)) {
            ms.when(CurrentUserProvider::getCurrentUser).thenReturn(CurrentUser.builder().uid("1").build());
            ts.when(TenantManager::tenantId).thenReturn(2L);
            // when
            listener.fillMetaData(entity);

            // then
            assertThat(entity.getCreatedBy()).isEqualTo("1");
            assertThat(entity.getUpdatedBy()).isEqualTo("1");
            assertThat(entity.getUpdatedDate()).isNotNull();
            assertThat(entity.getCreatedDate()).isNotNull();
            assertThat(entity.getTenantId()).isEqualTo(2L);
        }
    }

    @Test
    void fillMetaData2() {
        // given
        val listener = new AuditingEntityListener();
        val entity = new TestEntity2();
        try (val ms = Mockito.mockStatic(CurrentUserProvider.class); val ts = Mockito.mockStatic(TenantManager.class)) {
            ms.when(CurrentUserProvider::getCurrentUser).thenReturn(CurrentUser.builder().uid("1").build());
            ms.when(TenantManager::tenantId).thenReturn("2L");
            // when
            listener.fillMetaData(entity);

            // then
            assertThat(entity.getCreatedBy()).isEqualTo("1");
            assertThat(entity.getUpdatedBy()).isEqualTo("1");
            assertThat(entity.getUpdatedDate()).isNotNull();
            assertThat(entity.getCreatedDate()).isNotNull();
            assertThat(entity.getTenantId()).isEqualTo("2L");
        }
    }

    @Test
    void fillMetaData3() {
        // given
        val listener = new AuditingEntityListener();
        val entity = new TestEntity2();
        // when
        listener.fillMetaData(entity);
        // then
        assertThat(entity.getCreatedBy()).isNull();
        assertThat(entity.getUpdatedBy()).isNull();
        assertThat(entity.getUpdatedDate()).isNotNull();
        assertThat(entity.getCreatedDate()).isNotNull();
        assertThat(entity.getTenantId()).isNull();

    }

    @Test
    void saveData() {
        // given
        val entity = new TestEntity();
        try (val ms = Mockito.mockStatic(CurrentUserProvider.class); val ts = Mockito.mockStatic(TenantManager.class)) {
            ms.when(CurrentUserProvider::getCurrentUser).thenReturn(CurrentUser.builder().uid("1").build());
            ms.when(TenantManager::tenantId).thenReturn(2L);
            // when
            val e = testEntityRepository.save(entity);

            // then
            assertThat(e.getId()).startsWith("1_");
            assertThat(e.getCreatedBy()).isEqualTo("1");
            assertThat(e.getUpdatedBy()).isEqualTo("1");
            assertThat(e.getUpdatedDate()).isNotNull();
            assertThat(e.getCreatedDate()).isNotNull();
            assertThat(e.getTenantId()).isEqualTo(2L);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Entity
    static class TestEntity2 extends AuditableEntity<Long, String, Long> implements TenantAware<String> {

        @Id
        private Long id;

        private String createdBy;

        private Long createdDate;

        private String updatedBy;

        private Long updatedDate;

        private String tenantId;

    }

}

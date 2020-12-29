/*
 * Copyright 2020 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.snowdrop.boot.narayana.core.jdbc;

import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import me.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@ExtendWith(MockitoExtension.class)
class PooledXADataSourceWrapperTests {

    @Mock
    private XADataSource mockXaDataSource;

    @Mock
    private XARecoveryModule mockXaRecoveryModule;

    private Map<String, String> dbcpProperties;

    @Mock
    private RecoveryCredentialsProperties mockRecoveryCredentialsProperties;

    @Mock
    private TransactionManager mockTransactionManager;

    private PooledXADataSourceWrapper wrapper;

    @BeforeEach
    void before() {
        this.dbcpProperties = Collections.singletonMap("username", "test-user");
        this.wrapper = new PooledXADataSourceWrapper(this.mockTransactionManager, this.mockXaRecoveryModule,
               this.dbcpProperties, this.mockRecoveryCredentialsProperties);
    }

    @Test
    void wrap() throws Exception {
        given(this.mockRecoveryCredentialsProperties.isValid()).willReturn(false);

        DataSource dataSource = this.wrapper.wrapDataSource(this.mockXaDataSource);
        assertThat(dataSource).isInstanceOf(BasicManagedDataSource.class);

        BasicManagedDataSource basicManagedDataSource = (BasicManagedDataSource) dataSource;
        assertThat(basicManagedDataSource.getTransactionManager()).isEqualTo(this.mockTransactionManager);
        assertThat(basicManagedDataSource.getXaDataSourceInstance()).isEqualTo(this.mockXaDataSource);
        assertThat(basicManagedDataSource.getUsername()).isEqualTo("test-user");

        verify(this.mockXaRecoveryModule).addXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
        verify(this.mockRecoveryCredentialsProperties).isValid();
    }

    @Test
    void wrapWithCredentials() throws Exception {
        given(this.mockRecoveryCredentialsProperties.isValid()).willReturn(true);
        given(this.mockRecoveryCredentialsProperties.getUser()).willReturn("userName");
        given(this.mockRecoveryCredentialsProperties.getPassword()).willReturn("password");

        DataSource dataSource = this.wrapper.wrapDataSource(this.mockXaDataSource);
        assertThat(dataSource).isInstanceOf(BasicManagedDataSource.class);

        BasicManagedDataSource basicManagedDataSource = (BasicManagedDataSource) dataSource;
        assertThat(basicManagedDataSource.getTransactionManager()).isEqualTo(this.mockTransactionManager);
        assertThat(basicManagedDataSource.getXaDataSourceInstance()).isEqualTo(this.mockXaDataSource);

        verify(this.mockXaRecoveryModule).addXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
        verify(this.mockRecoveryCredentialsProperties).getUser();
        verify(this.mockRecoveryCredentialsProperties).getPassword();
    }
}

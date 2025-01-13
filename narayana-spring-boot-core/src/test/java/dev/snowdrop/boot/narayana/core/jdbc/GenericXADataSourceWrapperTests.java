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

package dev.snowdrop.boot.narayana.core.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import dev.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;
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
 * Tests for {@link GenericXADataSourceWrapper}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@ExtendWith(MockitoExtension.class)
class GenericXADataSourceWrapperTests {

    @Mock
    private XADataSource mockXaDataSource;

    @Mock
    private XAConnection mockXaConnection;

    @Mock
    private Connection mockConnection;

    @Mock
    private DatabaseMetaData mockDatabaseMetaData;

    @Mock
    private XARecoveryModule mockXaRecoveryModule;

    @Mock
    private RecoveryCredentialsProperties mockRecoveryCredentialsProperties;

    private GenericXADataSourceWrapper wrapper;

    @BeforeEach
    void before() throws SQLException {
        given(this.mockXaDataSource.getXAConnection()).willReturn(this.mockXaConnection);
        given(this.mockXaConnection.getConnection()).willReturn(this.mockConnection);
        given(this.mockConnection.getMetaData()).willReturn(this.mockDatabaseMetaData);
        given(this.mockDatabaseMetaData.getDatabaseProductName()).willReturn("");
        this.wrapper = new GenericXADataSourceWrapper(this.mockXaRecoveryModule, this.mockRecoveryCredentialsProperties);
    }

    @Test
    void wrap() throws Exception {
        given(this.mockRecoveryCredentialsProperties.isValid()).willReturn(false);
        DataSource wrapped = this.wrapper.wrapDataSource(this.mockXaDataSource);
        assertThat(wrapped).isInstanceOf(NarayanaDataSource.class);
        verify(this.mockXaRecoveryModule).addXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
        verify(this.mockRecoveryCredentialsProperties).isValid();
    }

    @Test
    void wrapWithCredentials() throws Exception {
        given(this.mockRecoveryCredentialsProperties.isValid()).willReturn(true);
        given(this.mockRecoveryCredentialsProperties.getUser()).willReturn("userName");
        given(this.mockRecoveryCredentialsProperties.getPassword()).willReturn("password");
        DataSource wrapped = this.wrapper.wrapDataSource(this.mockXaDataSource);
        assertThat(wrapped).isInstanceOf(NarayanaDataSource.class);
        verify(this.mockXaRecoveryModule).addXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
        verify(this.mockRecoveryCredentialsProperties).getUser();
        verify(this.mockRecoveryCredentialsProperties).getPassword();
    }
}

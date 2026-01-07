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

package dev.snowdrop.boot.narayana.core.jms;

import javax.transaction.xa.XAResource;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.ConnectionMetaData;
import jakarta.jms.Session;
import jakarta.jms.XAConnection;
import jakarta.jms.XAConnectionFactory;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import dev.snowdrop.boot.narayana.core.properties.MessagingHubConnectionFactoryProperties;
import dev.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;
import org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper;
import org.jboss.tm.FirstResource;
import org.jboss.tm.LastResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PooledXAConnectionFactoryWrapperTest {

    @Mock
    private XAConnectionFactory mockXaConnectionFactory;
    @Mock
    private TransactionManager mockTransactionManager;
    @Mock
    private XARecoveryModule mockXaRecoveryModule;
    @Spy
    private MessagingHubConnectionFactoryProperties spyMessagingHubConnectionFactoryProperties;
    @Mock
    private RecoveryCredentialsProperties mockRecoveryCredentialsProperties;
    private PooledXAConnectionFactoryWrapper wrapper;

    @BeforeEach
    void before() {
        this.wrapper = new PooledXAConnectionFactoryWrapper(this.mockTransactionManager, this.mockXaRecoveryModule,
                this.spyMessagingHubConnectionFactoryProperties, this.mockRecoveryCredentialsProperties);
    }

    @Test
    void wrap() throws Exception {
        given(this.mockRecoveryCredentialsProperties.isValid()).willReturn(false);
        ConnectionFactory connectionFactory = this.wrapper.wrapConnectionFactory(this.mockXaConnectionFactory);
        assertThat(connectionFactory).isInstanceOf(JmsPoolXAConnectionFactory.class);
        JmsPoolXAConnectionFactory pooledConnectionFactory = (JmsPoolXAConnectionFactory) connectionFactory;
        assertThat(pooledConnectionFactory.getTransactionManager()).isEqualTo(this.mockTransactionManager);
        assertThat(pooledConnectionFactory.getConnectionFactory()).isEqualTo(this.mockXaConnectionFactory);
        verify(this.mockXaRecoveryModule).addXAResourceRecoveryHelper(any(JmsXAResourceRecoveryHelper.class));
        verify(this.mockRecoveryCredentialsProperties).isValid();
    }

    @Test
    void wrapWithCredentials() throws Exception {
        given(this.mockRecoveryCredentialsProperties.isValid()).willReturn(true);
        given(this.mockRecoveryCredentialsProperties.getUser()).willReturn("userName");
        given(this.mockRecoveryCredentialsProperties.getPassword()).willReturn("password");
        ConnectionFactory connectionFactory = this.wrapper.wrapConnectionFactory(this.mockXaConnectionFactory);
        assertThat(connectionFactory).isInstanceOf(JmsPoolXAConnectionFactory.class);
        JmsPoolXAConnectionFactory pooledConnectionFactory = (JmsPoolXAConnectionFactory) connectionFactory;
        assertThat(pooledConnectionFactory.getTransactionManager()).isEqualTo(this.mockTransactionManager);
        assertThat(pooledConnectionFactory.getConnectionFactory()).isEqualTo(this.mockXaConnectionFactory);
        verify(this.mockXaRecoveryModule).addXAResourceRecoveryHelper(any(JmsXAResourceRecoveryHelper.class));
        verify(this.mockRecoveryCredentialsProperties).getUser();
        verify(this.mockRecoveryCredentialsProperties).getPassword();
    }

    @Test
    void wrapWithFirstResource() throws Exception {
        given(this.spyMessagingHubConnectionFactoryProperties.isFirstResource()).willReturn(true);
        given(this.mockRecoveryCredentialsProperties.isValid()).willReturn(false);
        ConnectionFactory connectionFactory = this.wrapper.wrapConnectionFactory(this.mockXaConnectionFactory);

        XAConnection mockXaConnection = mock(XAConnection.class);
        given(this.mockXaConnectionFactory.createXAConnection()).willReturn(mockXaConnection);
        given(mockXaConnection.getMetaData()).willReturn(mock(ConnectionMetaData.class));
        try (Connection connection = connectionFactory.createConnection()) {
            Transaction mockTransaction = mock(Transaction.class);
            given(this.mockTransactionManager.getTransaction()).willReturn(mockTransaction);
            ArgumentCaptor<XAResource> captorXaResource = ArgumentCaptor.captor();
            given(mockTransaction.enlistResource(captorXaResource.capture())).willReturn(true);
            try (Session ignored = connection.createSession()) {
                assertThat(captorXaResource.getValue()).isInstanceOf(FirstResource.class);
            }
        }
    }

    @Test
    void wrapWithLastResource() throws Exception {
        given(this.spyMessagingHubConnectionFactoryProperties.isLastResource()).willReturn(true);
        given(this.mockRecoveryCredentialsProperties.isValid()).willReturn(false);
        ConnectionFactory connectionFactory = this.wrapper.wrapConnectionFactory(this.mockXaConnectionFactory);

        XAConnection mockXaConnection = mock(XAConnection.class);
        given(this.mockXaConnectionFactory.createXAConnection()).willReturn(mockXaConnection);
        given(mockXaConnection.getMetaData()).willReturn(mock(ConnectionMetaData.class));
        try (Connection connection = connectionFactory.createConnection()) {
            Transaction mockTransaction = mock(Transaction.class);
            given(this.mockTransactionManager.getTransaction()).willReturn(mockTransaction);
            ArgumentCaptor<XAResource> captorXaResource = ArgumentCaptor.captor();
            given(mockTransaction.enlistResource(captorXaResource.capture())).willReturn(true);
            try (Session ignored = connection.createSession()) {
                assertThat(captorXaResource.getValue()).isInstanceOf(LastResource.class);
            }
        }
    }

    @Test
    void invalidFirstLastResourceConfiguration() throws Exception {
        given(this.spyMessagingHubConnectionFactoryProperties.isFirstResource()).willReturn(true);
        given(this.spyMessagingHubConnectionFactoryProperties.isLastResource()).willReturn(true);
        given(this.mockRecoveryCredentialsProperties.isValid()).willReturn(false);
        assertThatThrownBy(() -> this.wrapper.wrapConnectionFactory(this.mockXaConnectionFactory))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Setting both firstResource and lastResource is not allowed");
    }
}

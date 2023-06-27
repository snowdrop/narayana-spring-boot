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

package me.snowdrop.boot.narayana.core.jms;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.XAConnectionFactory;
import jakarta.transaction.TransactionManager;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import me.snowdrop.boot.narayana.core.properties.MessagingHubConnectionFactoryProperties;
import me.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;
import org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PooledXAConnectionFactoryWrapperTest {

    @Mock
    private XAConnectionFactory mockXaConnectionFactory;
    @Mock
    private TransactionManager mockTransactionManager;
    @Mock
    private XARecoveryModule mockXaRecoveryModule;
    private MessagingHubConnectionFactoryProperties messagingHubConnectionFactoryProperties;
    @Mock
    private RecoveryCredentialsProperties mockRecoveryCredentialsProperties;
    private PooledXAConnectionFactoryWrapper wrapper;

    @BeforeEach
    void before() {
        this.messagingHubConnectionFactoryProperties = new MessagingHubConnectionFactoryProperties();
        this.wrapper = new PooledXAConnectionFactoryWrapper(this.mockTransactionManager, this.mockXaRecoveryModule,
                this.messagingHubConnectionFactoryProperties, this.mockRecoveryCredentialsProperties);
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
}

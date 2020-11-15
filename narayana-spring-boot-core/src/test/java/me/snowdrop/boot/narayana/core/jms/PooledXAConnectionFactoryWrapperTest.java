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

import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import javax.transaction.TransactionManager;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import me.snowdrop.boot.narayana.core.properties.MessagingHubConnectionFactoryProperties;
import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import org.jboss.narayana.jta.jms.ConnectionFactoryProxy;
import org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PooledXAConnectionFactoryWrapperTest {

    @Mock
    private XAConnectionFactory mockXaConnectionFactory;
    @Mock
    private TransactionManager mockTransactionManager;
    @Mock
    private XARecoveryModule mockXaRecoveryModule;
    @Mock
    private NarayanaProperties mockNarayanaProperties;
    private PooledXAConnectionFactoryWrapper wrapper;

    @Before
    public void before() {
        this.wrapper = new PooledXAConnectionFactoryWrapper(this.mockTransactionManager, this.mockXaRecoveryModule,
                this.mockNarayanaProperties);
    }

    @Test
    public void wrap() throws Exception {
        given(this.mockNarayanaProperties.getMessaginghub()).willReturn(new MessagingHubConnectionFactoryProperties());
        ConnectionFactory connectionFactory = this.wrapper.wrapConnectionFactory(this.mockXaConnectionFactory);
        assertThat(connectionFactory).isInstanceOf(ConnectionFactoryProxy.class);
        JmsPoolXAConnectionFactory pooledConnectionFactory = (JmsPoolXAConnectionFactory) ReflectionTestUtils.getField(connectionFactory, "xaConnectionFactory");
        assertThat(pooledConnectionFactory.getTransactionManager()).isEqualTo(this.mockTransactionManager);
        assertThat(pooledConnectionFactory.getConnectionFactory()).isEqualTo(this.mockXaConnectionFactory);
        verify(this.mockXaRecoveryModule).addXAResourceRecoveryHelper(any(JmsXAResourceRecoveryHelper.class));
        verify(this.mockNarayanaProperties).getRecoveryJmsUser();
        verify(this.mockNarayanaProperties).getRecoveryJmsPass();
    }
}

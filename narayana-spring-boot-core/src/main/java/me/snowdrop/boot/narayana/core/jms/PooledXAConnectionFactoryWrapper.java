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
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import me.snowdrop.boot.narayana.core.jms.pool.JmsPoolNarayanaConnectionFactory;
import me.snowdrop.boot.narayana.core.jms.pool.NamedJmsXAResourceRecoveryHelper;
import me.snowdrop.boot.narayana.core.properties.MessagingHubConnectionFactoryProperties;
import me.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;

public class PooledXAConnectionFactoryWrapper extends AbstractXAConnectionFactoryWrapper {

    private final MessagingHubConnectionFactoryProperties properties;
    private final TransactionManager transactionManager;

    /**
     * Create a new {@link PooledXAConnectionFactoryWrapper} instance.
     *
     * @param transactionManager  underlying transaction manager
     * @param xaRecoveryModule    recovery module to register data source with.
     * @param properties          MessagingHub properties
     */
    public PooledXAConnectionFactoryWrapper(TransactionManager transactionManager, XARecoveryModule xaRecoveryModule,
            MessagingHubConnectionFactoryProperties properties) {
        this(transactionManager, xaRecoveryModule, properties, RecoveryCredentialsProperties.DEFAULT);
    }

    /**
     * Create a new {@link PooledXAConnectionFactoryWrapper} instance.
     *
     * @param transactionManager  underlying transaction manager
     * @param xaRecoveryModule    recovery module to register data source with.
     * @param properties          MessagingHub properties
     * @param recoveryCredentials Credentials for recovery helper
     */
    public PooledXAConnectionFactoryWrapper(TransactionManager transactionManager, XARecoveryModule xaRecoveryModule,
            MessagingHubConnectionFactoryProperties properties, RecoveryCredentialsProperties recoveryCredentials) {
        super(xaRecoveryModule, recoveryCredentials);
        this.properties = properties;
        this.transactionManager = transactionManager;
    }

    @Override
    protected ConnectionFactory wrapConnectionFactoryInternal(XAConnectionFactory xaConnectionFactory) {
        JmsPoolNarayanaConnectionFactory pooledConnectionFactory = new JmsPoolNarayanaConnectionFactory();
        pooledConnectionFactory.setName(this.properties.getName());
        pooledConnectionFactory.setTransactionManager(this.transactionManager);
        pooledConnectionFactory.setConnectionFactory(xaConnectionFactory);
        pooledConnectionFactory.setMaxConnections(this.properties.getMaxConnections());
        pooledConnectionFactory.setConnectionIdleTimeout((int) this.properties.getConnectionIdleTimeout().toMillis());
        pooledConnectionFactory.setConnectionCheckInterval(this.properties.getConnectionCheckInterval().toMillis());
        pooledConnectionFactory.setUseProviderJMSContext(this.properties.isUseProviderJMSContext());
        pooledConnectionFactory.setMaxSessionsPerConnection(this.properties.getMaxSessionsPerConnection());
        pooledConnectionFactory.setBlockIfSessionPoolIsFull(this.properties.isBlockIfSessionPoolIsFull());
        pooledConnectionFactory.setBlockIfSessionPoolIsFullTimeout(this.properties.getBlockIfSessionPoolIsFullTimeout().toMillis());
        pooledConnectionFactory.setUseAnonymousProducers(this.properties.isUseAnonymousProducers());
        return pooledConnectionFactory;
    }

    @Override
    protected XAResourceRecoveryHelper getRecoveryHelper(XAConnectionFactory xaConnectionFactory, RecoveryCredentialsProperties recoveryCredentials) {
        if (recoveryCredentials.isValid()) {
            return new NamedJmsXAResourceRecoveryHelper(xaConnectionFactory, recoveryCredentials.getUser(),
                recoveryCredentials.getPassword(), this.properties.getName());
        }
        return new NamedJmsXAResourceRecoveryHelper(xaConnectionFactory, this.properties.getName());
    }
}

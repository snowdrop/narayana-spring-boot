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
import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;

public class PooledXAConnectionFactoryWrapper extends AbstractXAConnectionFactoryWrapper {

    private final NarayanaProperties properties;
    private final TransactionManager transactionManager;

    public PooledXAConnectionFactoryWrapper(TransactionManager transactionManager, XARecoveryModule xaRecoveryModule, NarayanaProperties properties) {
        super(xaRecoveryModule, properties);
        this.properties = properties;
        this.transactionManager = transactionManager;
    }

    @Override
    protected ConnectionFactory wrapConnectionFactoryInternal(XAConnectionFactory xaConnectionFactory) throws Exception {
        JmsPoolXAConnectionFactory pooledConnectionFactory = new JmsPoolXAConnectionFactory();
        pooledConnectionFactory.setTransactionManager(this.transactionManager);
        pooledConnectionFactory.setConnectionFactory(xaConnectionFactory);

        pooledConnectionFactory.setMaxConnections(this.properties.getMessaginghub().getMaxConnections());
        pooledConnectionFactory.setConnectionIdleTimeout((int) this.properties.getMessaginghub().getConnectionIdleTimeout().toMillis());
        pooledConnectionFactory.setConnectionCheckInterval(this.properties.getMessaginghub().getConnectionCheckInterval().toMillis());
        pooledConnectionFactory.setUseProviderJMSContext(this.properties.getMessaginghub().isUseProviderJMSContext());
        pooledConnectionFactory.setMaxSessionsPerConnection(this.properties.getMessaginghub().getMaxSessionsPerConnection());
        pooledConnectionFactory.setBlockIfSessionPoolIsFull(this.properties.getMessaginghub().isBlockIfSessionPoolIsFull());
        pooledConnectionFactory.setBlockIfSessionPoolIsFullTimeout(this.properties.getMessaginghub().getBlockIfSessionPoolIsFullTimeout().toMillis());
        pooledConnectionFactory.setUseAnonymousProducers(this.properties.getMessaginghub().isUseAnonymousProducers());
        return pooledConnectionFactory;
    }
}

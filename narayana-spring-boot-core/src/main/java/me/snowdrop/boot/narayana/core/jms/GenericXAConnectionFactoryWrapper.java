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
import me.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;
import org.jboss.narayana.jta.jms.ConnectionFactoryProxy;
import org.jboss.narayana.jta.jms.TransactionHelperImpl;

public class GenericXAConnectionFactoryWrapper extends AbstractXAConnectionFactoryWrapper {

    private final TransactionManager transactionManager;

    /**
     * Create a new {@link GenericXAConnectionFactoryWrapper} instance.
     *
     * @param transactionManager  underlying transaction manager
     * @param xaRecoveryModule    recovery module to register data source with.
     */
    public GenericXAConnectionFactoryWrapper(TransactionManager transactionManager, XARecoveryModule xaRecoveryModule) {
        this(transactionManager, xaRecoveryModule, RecoveryCredentialsProperties.DEFAULT);
    }

    /**
     * Create a new {@link GenericXAConnectionFactoryWrapper} instance.
     *
     * @param transactionManager  underlying transaction manager
     * @param xaRecoveryModule    recovery module to register data source with.
     * @param recoveryCredentials Credentials for recovery helper
     */
    public GenericXAConnectionFactoryWrapper(TransactionManager transactionManager, XARecoveryModule xaRecoveryModule,
            RecoveryCredentialsProperties recoveryCredentials) {
        super(xaRecoveryModule, recoveryCredentials);
        this.transactionManager = transactionManager;
    }

    @Override
    protected ConnectionFactory wrapConnectionFactoryInternal(XAConnectionFactory xaConnectionFactory) {
        return new ConnectionFactoryProxy(xaConnectionFactory, new TransactionHelperImpl(this.transactionManager));
    }
}

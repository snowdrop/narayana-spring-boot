/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import org.jboss.narayana.jta.jms.ConnectionFactoryProxy;
import org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper;
import org.jboss.narayana.jta.jms.TransactionHelperImpl;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;

/**
 * {@link XAConnectionFactoryWrapper} implementation that uses {@link ConnectionFactoryProxy} to wrap an
 * {@link XAConnectionFactory}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class NarayanaXAConnectionFactoryWrapper implements XAConnectionFactoryWrapper {

    private final TransactionManager transactionManager;

    private final XARecoveryModule xaRecoveryModule;

    private final NarayanaProperties properties;

    /**
     * Create a new {@link NarayanaXAConnectionFactoryWrapper} instance.
     *
     * @param transactionManager underlying transaction manager
     * @param xaRecoveryModule    recovery module to register data source with.
     * @param properties         Narayana properties
     */
    public NarayanaXAConnectionFactoryWrapper(TransactionManager transactionManager, XARecoveryModule xaRecoveryModule,
            NarayanaProperties properties) {
        this.transactionManager = transactionManager;
        this.xaRecoveryModule = xaRecoveryModule;
        this.properties = properties;
    }

    @Override
    public ConnectionFactory wrapConnectionFactory(XAConnectionFactory xaConnectionFactory) {
        XAResourceRecoveryHelper recoveryHelper = getRecoveryHelper(xaConnectionFactory);
        this.xaRecoveryModule.addXAResourceRecoveryHelper(recoveryHelper);
        return new ConnectionFactoryProxy(xaConnectionFactory, new TransactionHelperImpl(this.transactionManager));
    }

    private XAResourceRecoveryHelper getRecoveryHelper(XAConnectionFactory xaConnectionFactory) {
        if (this.properties.getRecoveryJmsUser() == null && this.properties.getRecoveryJmsPass() == null) {
            return new JmsXAResourceRecoveryHelper(xaConnectionFactory);
        }
        return new JmsXAResourceRecoveryHelper(xaConnectionFactory, this.properties.getRecoveryJmsUser(),
                this.properties.getRecoveryJmsPass());
    }

}

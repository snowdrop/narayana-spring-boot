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

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import me.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;
import org.jboss.narayana.jta.jms.ConnectionFactoryProxy;
import org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;

/**
 * {@link XAConnectionFactoryWrapper} implementation that uses {@link ConnectionFactoryProxy} to wrap an
 * {@link XAConnectionFactory}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public abstract class AbstractXAConnectionFactoryWrapper implements XAConnectionFactoryWrapper {

    private final XARecoveryModule xaRecoveryModule;
    private final RecoveryCredentialsProperties recoveryCredentials;

    protected AbstractXAConnectionFactoryWrapper(XARecoveryModule xaRecoveryModule, RecoveryCredentialsProperties recoveryCredentials) {
        this.xaRecoveryModule = xaRecoveryModule;
        this.recoveryCredentials = recoveryCredentials;
    }

    protected abstract ConnectionFactory wrapConnectionFactoryInternal(XAConnectionFactory xaConnectionFactory);

    @Override
    public ConnectionFactory wrapConnectionFactory(XAConnectionFactory xaConnectionFactory) throws Exception {
        XAResourceRecoveryHelper recoveryHelper = getRecoveryHelper(xaConnectionFactory);
        this.xaRecoveryModule.addXAResourceRecoveryHelper(recoveryHelper);
        return wrapConnectionFactoryInternal(xaConnectionFactory);
    }

    private XAResourceRecoveryHelper getRecoveryHelper(XAConnectionFactory xaConnectionFactory) {
        if (this.recoveryCredentials.isValid()) {
            return new JmsXAResourceRecoveryHelper(xaConnectionFactory, this.recoveryCredentials.getUser(),
                this.recoveryCredentials.getPassword());
        }
        return new JmsXAResourceRecoveryHelper(xaConnectionFactory);
    }
}

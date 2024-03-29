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

import javax.sql.DataSource;
import javax.sql.XADataSource;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import dev.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;
import org.springframework.boot.jdbc.XADataSourceWrapper;

/**
 * An abstract {@link XADataSourceWrapper} implementation which handles {@link XAResourceRecoveryHelper} creation and
 * registration. It delegates the actual {@link XADataSource} wrapping to its subclasses.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public abstract class AbstractXADataSourceWrapper implements XADataSourceWrapper {

    private final XARecoveryModule xaRecoveryModule;
    private final RecoveryCredentialsProperties recoveryCredentials;

    protected AbstractXADataSourceWrapper(XARecoveryModule xaRecoveryModule, RecoveryCredentialsProperties recoveryCredentials) {
        this.xaRecoveryModule = xaRecoveryModule;
        this.recoveryCredentials = recoveryCredentials;
    }

    protected abstract DataSource wrapDataSourceInternal(XADataSource dataSource) throws Exception;

    /**
     * Register newly created recovery helper with the {@link XARecoveryModule} and delegate data source wrapping.
     *
     * @param dataSource {@link XADataSource} that needs to be wrapped.
     * @return wrapped data source
     * @throws Exception in case data source wrapping has failed
     */
    @Override
    public DataSource wrapDataSource(XADataSource dataSource) throws Exception {
        XAResourceRecoveryHelper recoveryHelper = getRecoveryHelper(dataSource);
        this.xaRecoveryModule.addXAResourceRecoveryHelper(recoveryHelper);
        return wrapDataSourceInternal(dataSource);
    }

    private XAResourceRecoveryHelper getRecoveryHelper(XADataSource dataSource) {
        if (this.recoveryCredentials.isValid()) {
            return new DataSourceXAResourceRecoveryHelper(dataSource, this.recoveryCredentials.getUser(),
                this.recoveryCredentials.getPassword());
        }
        return new DataSourceXAResourceRecoveryHelper(dataSource);
    }

}

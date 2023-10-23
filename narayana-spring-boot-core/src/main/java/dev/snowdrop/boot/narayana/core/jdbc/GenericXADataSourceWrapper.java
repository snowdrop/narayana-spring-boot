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
import dev.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;

/**
 * {@link AbstractXADataSourceWrapper} implementation that uses {@link NarayanaDataSource} to wrap an
 * {@link XADataSource}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class GenericXADataSourceWrapper extends AbstractXADataSourceWrapper {

    /**
     * Create a new {@link GenericXADataSourceWrapper} instance.
     *
     * @param xaRecoveryModule    recovery module to register data source with.
     */
    public GenericXADataSourceWrapper(XARecoveryModule xaRecoveryModule) {
        this(xaRecoveryModule, RecoveryCredentialsProperties.DEFAULT);
    }

    /**
     * Create a new {@link GenericXADataSourceWrapper} instance.
     *
     * @param xaRecoveryModule    recovery module to register data source with.
     * @param recoveryCredentials credentials for recovery helper
     */
    public GenericXADataSourceWrapper(XARecoveryModule xaRecoveryModule, RecoveryCredentialsProperties recoveryCredentials) {
        super(xaRecoveryModule, recoveryCredentials);
    }

    /**
     * Wrap provided {@link XADataSource} with an instance of {@link NarayanaDataSource}.
     *
     * @param dataSource data source that needs to be wrapped.
     * @return wrapped data source.
     */
    @Override
    protected DataSource wrapDataSourceInternal(XADataSource dataSource) {
        return new NarayanaDataSource(dataSource);
    }

}

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

package me.snowdrop.boot.narayana.core.jdbc;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;

/**
 * XADataSource wrapper that uses {@link NarayanaDataSource} to wrap an {@link XADataSource}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class NarayanaXADataSourceWrapper {

    private final NarayanaProperties properties;

    private final XARecoveryModule xaRecoveryModule;

    /**
     * Create a new {@link NarayanaXADataSourceWrapper} instance.
     *
     * @param properties       Narayana properties.
     * @param xaRecoveryModule recovery module to register data source with.
     */
    public NarayanaXADataSourceWrapper(NarayanaProperties properties, XARecoveryModule xaRecoveryModule) {
        this.properties = properties;
        this.xaRecoveryModule = xaRecoveryModule;
    }

    public DataSource wrapDataSource(XADataSource dataSource) {
        XAResourceRecoveryHelper recoveryHelper = getRecoveryHelper(dataSource);
        this.xaRecoveryModule.addXAResourceRecoveryHelper(recoveryHelper);
        return new NarayanaDataSource(dataSource);
    }

    private XAResourceRecoveryHelper getRecoveryHelper(XADataSource dataSource) {
        if (this.properties.getRecoveryDbUser() == null && this.properties.getRecoveryDbPass() == null) {
            return new DataSourceXAResourceRecoveryHelper(dataSource);
        }
        return new DataSourceXAResourceRecoveryHelper(dataSource, this.properties.getRecoveryDbUser(),
                this.properties.getRecoveryDbPass());
    }

}

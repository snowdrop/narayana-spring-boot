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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.IsSameRMModifier;
import com.arjuna.ats.internal.jdbc.drivers.modifiers.ModifierFactory;
import com.arjuna.ats.internal.jdbc.drivers.modifiers.SupportsMultipleConnectionsModifier;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import dev.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;
import dev.snowdrop.boot.narayana.core.properties.TransactionalDriverProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * An {@link XADataSourceWrapper} implementation which handles {@link XAResourceRecoveryHelper} creation and
 * registration. It delegates the actual {@link XADataSource} wrapping to its subclass {@link NarayanaDataSource}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class GenericXADataSourceWrapper implements XADataSourceWrapper {

    private final XARecoveryModule xaRecoveryModule;
    private final TransactionalDriverProperties transactionalDriverProperties;
    private final RecoveryCredentialsProperties recoveryCredentials;

    /**
     * Create a new {@link GenericXADataSourceWrapper} instance.
     *
     * @param xaRecoveryModule              recovery module to register data source with.
     */
    public GenericXADataSourceWrapper(XARecoveryModule xaRecoveryModule) {
        this(xaRecoveryModule, RecoveryCredentialsProperties.DEFAULT);
    }

    /**
     * Create a new {@link GenericXADataSourceWrapper} instance.
     *
     * @param xaRecoveryModule              recovery module to register data source with.
     * @param recoveryCredentials           credentials for recovery helper
     */
    public GenericXADataSourceWrapper(XARecoveryModule xaRecoveryModule, RecoveryCredentialsProperties recoveryCredentials) {
        this(xaRecoveryModule, new TransactionalDriverProperties(), recoveryCredentials);
    }

    /**
     * Create a new {@link GenericXADataSourceWrapper} instance.
     *
     * @param xaRecoveryModule              recovery module to register data source with.
     * @param transactionalDriverProperties Transactional driver properties
     */
    public GenericXADataSourceWrapper(XARecoveryModule xaRecoveryModule, TransactionalDriverProperties transactionalDriverProperties) {
        this(xaRecoveryModule, transactionalDriverProperties, RecoveryCredentialsProperties.DEFAULT);
    }

    /**
     * Create a new {@link GenericXADataSourceWrapper} instance.
     *
     * @param xaRecoveryModule              recovery module to register data source with.
     * @param transactionalDriverProperties Transactional driver properties
     * @param recoveryCredentials           credentials for recovery helper
     */
    public GenericXADataSourceWrapper(XARecoveryModule xaRecoveryModule, TransactionalDriverProperties transactionalDriverProperties,
                                      RecoveryCredentialsProperties recoveryCredentials) {
        this.xaRecoveryModule = xaRecoveryModule;
        this.transactionalDriverProperties = transactionalDriverProperties;
        this.recoveryCredentials = recoveryCredentials;
    }

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
        registerModifier(dataSource);
        return new NarayanaDataSource(dataSource, this.transactionalDriverProperties);
    }

    private XAResourceRecoveryHelper getRecoveryHelper(XADataSource dataSource) {
        if (this.recoveryCredentials.isValid()) {
            return new DataSourceXAResourceRecoveryHelper(dataSource, this.recoveryCredentials.getUser(),
                    this.recoveryCredentials.getPassword(), this.transactionalDriverProperties.getName());
        }
        return new DataSourceXAResourceRecoveryHelper(dataSource, this.transactionalDriverProperties.getName());
    }

    private void registerModifier(XADataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getXAConnection().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String driver = metaData.getDriverName();
            int major = metaData.getDriverMajorVersion();
            int minor = metaData.getDriverMinorVersion();
            switch (this.transactionalDriverProperties.getModifier()) {
                case DEFAULT -> {
                    switch (DatabaseDriver.fromProductName(JdbcUtils.commonDatabaseName(metaData.getDatabaseProductName()))) {
                        case DB2, H2, MYSQL, ORACLE, SQLSERVER -> ModifierFactory.putModifier(driver, major, minor, IsSameRMModifier.class.getName());
                        case POSTGRESQL -> ModifierFactory.putModifier(driver, major, minor, SupportsMultipleConnectionsModifier.class.getName());
                    }
                }
                case IS_SAME_RM -> ModifierFactory.putModifier(driver, major, minor, IsSameRMModifier.class.getName());
                case SUPPORTS_MULTIPLE_CONNECTIONS -> ModifierFactory.putModifier(driver, major, minor, SupportsMultipleConnectionsModifier.class.getName());
            }
        }
    }
}

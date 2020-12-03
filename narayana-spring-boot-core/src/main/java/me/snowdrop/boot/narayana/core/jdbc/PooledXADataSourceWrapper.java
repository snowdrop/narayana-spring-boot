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

package me.snowdrop.boot.narayana.core.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import me.snowdrop.boot.narayana.core.properties.RecoveryCredentialsProperties;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;

/**
 * {@link AbstractXADataSourceWrapper} implementation that uses {@link BasicManagedDataSource} to wrap an
 * {@link XADataSource}.
 * <p>
 * {@link BasicManagedDataSource} provides a pooling support which is not available in the Narayana transactional
 * driver.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class PooledXADataSourceWrapper extends AbstractXADataSourceWrapper {

    private final Map<String, String> properties;
    private final TransactionManager transactionManager;

    /**
     * Create a new {@link PooledXADataSourceWrapper} instance.
     *
     * @param transactionManager  underlying transaction manager
     * @param xaRecoveryModule    recovery module to register data source with.
     * @param properties          DBCP properties
     */
    public PooledXADataSourceWrapper(TransactionManager transactionManager, XARecoveryModule xaRecoveryModule,
            Map<String, String> properties) {
        this(transactionManager, xaRecoveryModule, properties, RecoveryCredentialsProperties.DEFAULT);
    }

    /**
     * Create a new {@link PooledXADataSourceWrapper} instance.
     *
     * @param transactionManager  underlying transaction manager
     * @param xaRecoveryModule    recovery module to register data source with.
     * @param properties          DBCP properties
     * @param recoveryCredentials credentials for recovery helper
     */
    public PooledXADataSourceWrapper(TransactionManager transactionManager, XARecoveryModule xaRecoveryModule,
            Map<String, String> properties, RecoveryCredentialsProperties recoveryCredentials) {
        super(xaRecoveryModule, recoveryCredentials);
        this.properties = properties;
        this.transactionManager = transactionManager;
    }

    /**
     * Wrap the provided data source and initialize the connection pool if its initial size is higher than 0.
     *
     * @param xaDataSource data source that needs to be wrapped
     * @return wrapped data source
     * @throws Exception if data source copy or connection pool initialization has failed.
     */
    @Override
    protected DataSource wrapDataSourceInternal(XADataSource xaDataSource) throws Exception {
        BasicManagedDataSource basicManagedDataSource = new BasicManagedDataSource();
        // Managed data source does't have a factory. Therefore we need to create an unmanaged data source and then copy
        // it's configuration to the managed one.
        BasicDataSource basicDataSource = getBasicDataSource();
        copyFields(basicDataSource, basicManagedDataSource);
        basicManagedDataSource.setTransactionManager(this.transactionManager);
        basicManagedDataSource.setXaDataSourceInstance(xaDataSource);

        // Initialize the connections pool
        int initialSize = Integer.valueOf(this.properties.getOrDefault("initialSize", "0"));
        if (initialSize > 0) {
            basicManagedDataSource.setInitialSize(initialSize);
            basicManagedDataSource.getLogWriter(); // A trick to trigger pool initialization
        }

        return basicManagedDataSource;
    }

    private BasicDataSource getBasicDataSource() throws Exception {
        Properties dbcpProperties = new Properties();
        dbcpProperties.putAll(this.properties);
        // BasicDataSource is only used to load correct properties. Thus no connections should be created.
        dbcpProperties.put("initialSize", "0");

        return BasicDataSourceFactory.createDataSource(dbcpProperties);
    }

    private void copyFields(Object source, Object destination) throws IllegalAccessException {
        for (Field field: source.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(source) == null || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            field.set(destination, field.get(source));
        }
    }

}

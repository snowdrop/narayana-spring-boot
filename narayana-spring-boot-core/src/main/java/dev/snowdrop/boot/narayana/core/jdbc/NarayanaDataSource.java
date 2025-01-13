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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import com.arjuna.ats.internal.jdbc.ConnectionManager;
import com.arjuna.ats.jdbc.TransactionalDriver;
import dev.snowdrop.boot.narayana.core.properties.TransactionalDriverProperties;

/**
 * {@link DataSource} implementation wrapping {@link XADataSource} and using
 * {@link ConnectionManager} to acquire connections.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class NarayanaDataSource implements DataSource {

    private final XADataSource xaDataSource;
    private final TransactionalDriverProperties transactionalDriverProperties;

    /**
     * Create a new {@link NarayanaDataSource} instance.
     *
     * @param xaDataSource                    the XA DataSource
     * @param transactionalDriverProperties   Transactional driver pool properties
     */
    public NarayanaDataSource(XADataSource xaDataSource, TransactionalDriverProperties transactionalDriverProperties) {
        this.xaDataSource = xaDataSource;
        this.transactionalDriverProperties = transactionalDriverProperties;
    }

    private Properties createProperties() {
        Properties properties = new Properties();
        properties.put(TransactionalDriver.XADataSource, this.xaDataSource);
        properties.put(TransactionalDriver.poolConnections, String.valueOf(this.transactionalDriverProperties.getPool().isEnabled()));
        properties.put(TransactionalDriver.maxConnections, this.transactionalDriverProperties.getPool().getMaxConnections());
        return properties;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ConnectionManager.create(this.transactionalDriverProperties.getName(), createProperties());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Properties properties = createProperties();
        properties.put(TransactionalDriver.userName, username);
        properties.put(TransactionalDriver.password, password);
        return ConnectionManager.create(this.transactionalDriverProperties.getName(), properties);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return this.xaDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.xaDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.xaDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.xaDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (isWrapperFor(iface)) {
            return (T) this;
        }
        if (iface.isAssignableFrom(this.xaDataSource.getClass())) {
            return (T) this.xaDataSource;
        }
        throw new SQLException(getClass() + " is not a wrapper for " + iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(getClass());
    }

}

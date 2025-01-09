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

package com.arjuna.ats.internal.jdbc;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import jakarta.transaction.Transaction;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.ConnectionModifier;
import com.arjuna.ats.jdbc.logging.jdbcLogger;
import dev.snowdrop.boot.narayana.core.jdbc.NamedXAResource;

public class ProvidedXADataSourceConnection implements ConnectionControl, TransactionalDriverXAConnection {

    private final BaseTransactionalDriverXAConnection delegate = new BaseTransactionalDriverXAConnection() {
    };

    public ProvidedXADataSourceConnection(String dbName, String user, String passwd, XADataSource xaDatasource, ConnectionImple conn) {
        if (jdbcLogger.logger.isTraceEnabled()) {
            jdbcLogger.logger.trace("ProvidedXADataSourceConnection.ProvidedXADataSourceConnection( " + dbName + ", " + user + ", " + passwd + ", " + xaDatasource + " )");
        }
        this.delegate._dbName = dbName;
        this.delegate._user = user;
        this.delegate._passwd = passwd;
        this.delegate._theDataSource = xaDatasource;
        this.delegate._theArjunaConnection = conn;
    }

    @Override
    public String dynamicClass() {
        return this.delegate.dynamicClass();
    }

    @Override
    public String dataSourceName() {
        return this.delegate.dataSourceName();
    }

    @Override
    public String password() {
        return this.delegate.password();
    }

    @Override
    public void setModifier(ConnectionModifier cm) {
        this.delegate.setModifier(cm);
    }

    @Override
    public Transaction transaction() {
        return this.delegate.transaction();
    }

    @Override
    public String url() {
        return this.delegate.url();
    }

    @Override
    public String user() {
        return this.delegate.user();
    }

    @Override
    public XADataSource xaDataSource() {
        return this.delegate.xaDataSource();
    }

    @Override
    public void closeCloseCurrentConnection() throws SQLException {
        this.delegate.closeCloseCurrentConnection();
    }

    @Override
    public XAConnection getConnection() throws SQLException {
        return this.delegate.getConnection();
    }

    @Override
    public XAResource getResource() throws SQLException {
        if (this.delegate._theXAResource == null) {
            this.delegate._theXAResource = new NamedXAResource(this.delegate.getResource(), this.delegate.dataSourceName());
        }
        return this.delegate._theXAResource;
    }

    @Override
    public boolean inuse() {
        return this.delegate.inuse();
    }

    @Override
    public boolean setTransaction(Transaction tx) {
        return this.delegate.setTransaction(tx);
    }

    @Override
    public boolean validTransaction(Transaction tx) {
        return this.delegate.validTransaction(tx);
    }
}

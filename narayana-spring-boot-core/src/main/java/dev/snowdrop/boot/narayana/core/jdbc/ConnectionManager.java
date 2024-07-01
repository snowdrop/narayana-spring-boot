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

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;

public class ConnectionManager {

    private final XADataSource xaDataSource;
    private final String user;
    private final String password;
    private XAConnection xaConnection;

    public ConnectionManager(XADataSource xaDataSource, String user, String password) {
        this.xaDataSource = xaDataSource;
        this.user = user;
        this.password = password;
    }

    public void connectAndAccept(XAResourceConsumer consumer) throws XAException {
        if (isConnected()) {
            try {
                consumer.accept(this.xaConnection.getXAResource());
            } catch (SQLException ex) {
                throw createXAException(ex.getMessage());
            }
            return;
        }

        connect();
        try {
            consumer.accept(this.xaConnection.getXAResource());
        } catch (SQLException ex) {
            throw createXAException(ex.getMessage());
        } finally {
            disconnect();
        }
    }

    public <T> T connectAndApply(XAResourceFunction<T> function) throws XAException {
        if (isConnected()) {
            try {
                return function.apply(this.xaConnection.getXAResource());
            } catch (SQLException ex) {
                throw createXAException(ex.getMessage());
            }
        }

        connect();
        try {
            return function.apply(this.xaConnection.getXAResource());
        } catch (SQLException ex) {
            throw createXAException(ex.getMessage());
        } finally {
            disconnect();
        }
    }

    public void connect() throws XAException {
        if (isConnected()) {
            return;
        }

        try {
            this.xaConnection = createXAConnection();
        } catch (SQLException ex) {
            if (this.xaConnection != null) {
                try {
                    this.xaConnection.close();
                } catch (SQLException ignore) {
                }
            }
            throw createXAException(ex.getMessage());
        }
    }

    public void disconnect() {
        if (!isConnected()) {
            return;
        }

        try {
            this.xaConnection.close();
        } catch (SQLException e) {
        } finally {
            this.xaConnection = null;
        }
    }

    public boolean isConnected() {
        return this.xaConnection != null;
    }

    private XAConnection createXAConnection() throws SQLException {
        if (this.user == null && this.password == null) {
            return this.xaDataSource.getXAConnection();
        }

        return this.xaDataSource.getXAConnection(this.user, this.password);
    }

    private XAException createXAException(String message) {
        XAException xaException = new XAException(message);
        xaException.errorCode = XAException.XAER_RMFAIL;
        return xaException;
    }
}

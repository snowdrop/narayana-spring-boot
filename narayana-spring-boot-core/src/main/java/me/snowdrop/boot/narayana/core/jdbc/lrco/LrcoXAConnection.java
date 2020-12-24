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

package me.snowdrop.boot.narayana.core.jdbc.lrco;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

public class LrcoXAConnection implements XAConnection {

    private final Connection physicalConnection;
    private volatile Connection handleConnection;
    private final List<ConnectionEventListener> eventListeners;

    public LrcoXAConnection(Connection connection) throws SQLException {
        this.physicalConnection = connection;
        this.eventListeners = new ArrayList<>();
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        return new LrcoXAResource(this.physicalConnection);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection lastHandleConnection = this.handleConnection;
        if (lastHandleConnection != null) {
            lastHandleConnection.close();
        }
        this.physicalConnection.rollback();
        this.handleConnection = new PooledJdbcConnection();
        return this.handleConnection;
    }

    @Override
    public void close() throws SQLException {
        Connection lastHandleConnection = this.handleConnection;
        if (lastHandleConnection != null) {
            lastHandleConnection.close();
        }
        this.physicalConnection.close();
    }

    private void closeHandle() {
        ConnectionEvent event = new ConnectionEvent(this);
        for (int i = 0; i < this.eventListeners.size(); i++) {
            ConnectionEventListener listener = this.eventListeners.get(i);
            listener.connectionClosed(event);
        }
        this.handleConnection = null;
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        if (!this.eventListeners.contains(listener)) {
            this.eventListeners.add(listener);
        }
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        this.eventListeners.remove(listener);
    }

    @Override
    public void addStatementEventListener(StatementEventListener listener) {
    }

    @Override
    public void removeStatementEventListener(StatementEventListener listener) {
    }

    private final class PooledJdbcConnection implements Connection {

        private boolean closed;

        private PooledJdbcConnection() {
            this.closed = false;
        }

        @Override
        public void close() throws SQLException {
            if (!this.closed) {
                try {
                    LrcoXAConnection.this.physicalConnection.rollback();
                    LrcoXAConnection.this.physicalConnection.setAutoCommit(true);
                } catch (SQLException e) {
                    // ignore
                }
                closeHandle();
                this.closed = true;
            }
        }

        @Override
        public Statement createStatement() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.createStatement();
        }

        @Override
        public PreparedStatement prepareStatement(String string) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.prepareStatement(string);
        }

        @Override
        public CallableStatement prepareCall(String string) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.prepareCall(string);
        }

        @Override
        public String nativeSQL(String string) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.nativeSQL(string);
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            LrcoXAConnection.this.physicalConnection.setAutoCommit(autoCommit);
        }

        @Override
        public boolean getAutoCommit() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getAutoCommit();
        }

        @Override
        public void commit() throws SQLException {
            LrcoXAConnection.this.physicalConnection.commit();
        }

        @Override
        public void rollback() throws SQLException {
            LrcoXAConnection.this.physicalConnection.rollback();
        }

        @Override
        public boolean isClosed() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.isClosed();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getMetaData();
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            LrcoXAConnection.this.physicalConnection.setReadOnly(readOnly);
        }

        @Override
        public boolean isReadOnly() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.isReadOnly();
        }

        @Override
        public void setCatalog(String string) throws SQLException {
            LrcoXAConnection.this.physicalConnection.setCatalog(string);
        }

        @Override
        public String getCatalog() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getCatalog();
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            LrcoXAConnection.this.physicalConnection.setTransactionIsolation(level);
        }

        @Override
        public int getTransactionIsolation() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getTransactionIsolation();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getWarnings();
        }

        @Override
        public void clearWarnings() throws SQLException {
            LrcoXAConnection.this.physicalConnection.clearWarnings();
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.createStatement(resultSetType, resultSetConcurrency);
        }

        @Override
        public PreparedStatement prepareStatement(String string, int i, int i1) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.prepareStatement(string, i, i1);
        }

        @Override
        public CallableStatement prepareCall(String string, int i, int i1) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.prepareCall(string, i, i1);
        }

        @Override
        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getTypeMap();
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
            LrcoXAConnection.this.physicalConnection.setTypeMap(map);
        }

        @Override
        public void setHoldability(int holdability) throws SQLException {
            LrcoXAConnection.this.physicalConnection.setHoldability(holdability);
        }

        @Override
        public int getHoldability() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getHoldability();
        }

        @Override
        public Savepoint setSavepoint() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.setSavepoint();
        }

        @Override
        public Savepoint setSavepoint(String string) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.setSavepoint(string);
        }

        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            LrcoXAConnection.this.physicalConnection.rollback(savepoint);
        }

        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            LrcoXAConnection.this.physicalConnection.releaseSavepoint(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        @Override
        public PreparedStatement prepareStatement(String string, int i, int i1, int i2) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.prepareStatement(string, i, i1, i2);
        }

        @Override
        public CallableStatement prepareCall(String string, int i, int i1, int i2) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.prepareCall(string, i, i1, i2);
        }

        @Override
        public PreparedStatement prepareStatement(String string, int i) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.prepareStatement(string, i);
        }

        @Override
        public PreparedStatement prepareStatement(String string, int[] ints) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.prepareStatement(string, ints);
        }

        @Override
        public PreparedStatement prepareStatement(String string, String[] strings) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.prepareStatement(string, strings);
        }

        @Override
        public Clob createClob() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.createClob();
        }

        @Override
        public Blob createBlob() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.createBlob();
        }

        @Override
        public NClob createNClob() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.createNClob();
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.createSQLXML();
        }

        @Override
        public boolean isValid(int timeout) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.isValid(timeout);
        }

        @Override
        public void setClientInfo(String string, String string1) throws SQLClientInfoException {
            LrcoXAConnection.this.physicalConnection.setClientInfo(string, string1);
        }

        @Override
        public void setClientInfo(Properties prprts) throws SQLClientInfoException {
            LrcoXAConnection.this.physicalConnection.setClientInfo(prprts);
        }

        @Override
        public String getClientInfo(String string) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getClientInfo(string);
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getClientInfo();
        }

        @Override
        public Array createArrayOf(String string, Object[] os) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.createArrayOf(string, os);
        }

        @Override
        public Struct createStruct(String string, Object[] os) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.createStruct(string, os);
        }

        @Override
        public void setSchema(String string) throws SQLException {
            LrcoXAConnection.this.physicalConnection.setSchema(string);
        }

        @Override
        public String getSchema() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getSchema();
        }

        @Override
        public void abort(Executor exctr) throws SQLException {
            LrcoXAConnection.this.physicalConnection.abort(exctr);
        }

        @Override
        public void setNetworkTimeout(Executor exctr, int i) throws SQLException {
            LrcoXAConnection.this.physicalConnection.setNetworkTimeout(exctr, i);
        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return LrcoXAConnection.this.physicalConnection.getNetworkTimeout();
        }

        @Override
        public <T> T unwrap(Class<T> type) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.unwrap(type);
        }

        @Override
        public boolean isWrapperFor(Class<?> type) throws SQLException {
            return LrcoXAConnection.this.physicalConnection.isWrapperFor(type);
        }
    }
}

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

import java.io.PrintWriter;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class LrcoXADataSource implements XADataSource {

    private final Driver driver;
    private final String url;
    private final Properties info;

    public LrcoXADataSource(Driver driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.info = new Properties();
        if (username != null) {
            this.info.setProperty("user", username);
        }
        if (password != null) {
            this.info.setProperty("password", password);
        }
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return new LrcoXAConnection(this.driver.connect(this.url, this.info));
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        Properties info = new Properties(this.info);
        info.put("user", user);
        info.put("password", password);
        return new LrcoXAConnection(this.driver.connect(this.url, info));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("getLogWriter");
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException("setLogWriter");
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException("setLoginTimeout");
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.driver.getParentLogger();
    }
}

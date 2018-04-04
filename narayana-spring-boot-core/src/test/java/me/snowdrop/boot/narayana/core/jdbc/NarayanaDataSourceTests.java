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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import com.arjuna.ats.internal.jdbc.ConnectionImple;
import com.arjuna.ats.jdbc.TransactionalDriver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link NarayanaDataSource}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class NarayanaDataSourceTests {

    @Mock
    private XADataSource mockXaDataSource;

    private NarayanaDataSource dataSourceBean;

    @Before
    public void before() {
        this.dataSourceBean = new NarayanaDataSource(this.mockXaDataSource);
    }

    @Test
    public void shouldBeAWrapper() {
        assertThat(this.dataSourceBean.isWrapperFor(DataSource.class)).isTrue();
    }

    @Test
    public void shouldNotBeAWrapper() {
        assertThat(this.dataSourceBean.isWrapperFor(XADataSource.class)).isFalse();
    }

    @Test
    public void shouldUnwrapDataSource() throws SQLException {
        assertThat(this.dataSourceBean.unwrap(DataSource.class)).isInstanceOf(DataSource.class);
        assertThat(this.dataSourceBean.unwrap(DataSource.class)).isSameAs(this.dataSourceBean);
    }

    @Test
    public void shouldUnwrapXaDataSource() throws SQLException {
        assertThat(this.dataSourceBean.unwrap(XADataSource.class)).isInstanceOf(XADataSource.class);
        assertThat(this.dataSourceBean.unwrap(XADataSource.class)).isSameAs(this.mockXaDataSource);
    }

    @Test
    public void shouldGetConnectionAndCommit() throws SQLException {
        Connection mockConnection = mock(Connection.class);
        XAConnection mockXaConnection = mock(XAConnection.class);
        given(mockXaConnection.getConnection()).willReturn(mockConnection);
        given(this.mockXaDataSource.getXAConnection()).willReturn(mockXaConnection);

        // TODO properties not used
        Properties properties = new Properties();
        properties.put(TransactionalDriver.XADataSource, this.mockXaDataSource);

        Connection connection = this.dataSourceBean.getConnection();
        assertThat(connection).isInstanceOf(ConnectionImple.class);

        connection.commit();

        verify(this.mockXaDataSource, times(1)).getXAConnection();
        verify(mockXaConnection, times(1)).getConnection();
        verify(mockConnection, times(1)).commit();
    }

    @Test
    public void shouldGetConnectionAndCommitWithCredentials() throws SQLException {
        String username = "testUsername";
        String password = "testPassword";
        Connection mockConnection = mock(Connection.class);
        XAConnection mockXaConnection = mock(XAConnection.class);
        given(mockXaConnection.getConnection()).willReturn(mockConnection);
        given(this.mockXaDataSource.getXAConnection(username, password)).willReturn(mockXaConnection);

        // TODO properties not used
        Properties properties = new Properties();
        properties.put(TransactionalDriver.XADataSource, this.mockXaDataSource);
        properties.put(TransactionalDriver.userName, username);
        properties.put(TransactionalDriver.password, password);

        Connection connection = this.dataSourceBean.getConnection(username, password);
        assertThat(connection).isInstanceOf(ConnectionImple.class);

        connection.commit();

        verify(this.mockXaDataSource, times(1)).getXAConnection(username, password);
        verify(mockXaConnection, times(1)).getConnection();
        verify(mockConnection, times(1)).commit();
    }

}

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

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link DataSourceXAResourceRecoveryHelper}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSourceXAResourceRecoveryHelperTests {

    @Mock
    private XADataSource mockXaDataSource;

    @Mock
    private XAConnection mockXaConnection;

    @Mock
    private XAResource mockXaResource;

    private DataSourceXAResourceRecoveryHelper recoveryHelper;

    @Before
    public void before() throws SQLException {
        this.recoveryHelper = new DataSourceXAResourceRecoveryHelper(this.mockXaDataSource);

        given(this.mockXaDataSource.getXAConnection()).willReturn(this.mockXaConnection);
        given(this.mockXaConnection.getXAResource()).willReturn(this.mockXaResource);
    }

    @Test
    public void shouldCreateConnectionAndGetXAResource() throws SQLException {
        XAResource[] xaResources = this.recoveryHelper.getXAResources();
        assertThat(xaResources.length).isEqualTo(1);
        assertThat(xaResources[0]).isSameAs(this.recoveryHelper);
        verify(this.mockXaDataSource).getXAConnection();
        verify(this.mockXaConnection).getXAResource();
    }

    @Test
    public void shouldCreateConnectionWithCredentialsAndGetXAResource() throws SQLException {
        given(this.mockXaDataSource.getXAConnection(anyString(), anyString())).willReturn(this.mockXaConnection);
        this.recoveryHelper = new DataSourceXAResourceRecoveryHelper(this.mockXaDataSource, "username", "password");
        XAResource[] xaResources = this.recoveryHelper.getXAResources();
        assertThat(xaResources.length).isEqualTo(1);
        assertThat(xaResources[0]).isSameAs(this.recoveryHelper);
        verify(this.mockXaDataSource).getXAConnection("username", "password");
        verify(this.mockXaConnection).getXAResource();
    }

    @Test
    public void shouldFailToCreateConnectionAndNotGetXAResource() throws SQLException {
        given(this.mockXaDataSource.getXAConnection()).willThrow(new SQLException("Test exception"));
        XAResource[] xaResources = this.recoveryHelper.getXAResources();
        assertThat(xaResources.length).isEqualTo(0);
        verify(this.mockXaDataSource).getXAConnection();
        verify(this.mockXaConnection, times(0)).getXAResource();
    }

    @Test
    public void shouldDelegateRecoverCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.recover(XAResource.TMSTARTRSCAN);
        verify(this.mockXaResource).recover(XAResource.TMSTARTRSCAN);
    }

    @Test
    public void shouldDelegateRecoverCallAndCloseConnection() throws XAException, SQLException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.recover(XAResource.TMENDRSCAN);
        verify(this.mockXaResource).recover(XAResource.TMENDRSCAN);
        verify(this.mockXaConnection).close();
    }

    @Test
    public void shouldDelegateStartCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.start(null, 0);
        verify(this.mockXaResource).start(null, 0);
    }

    @Test
    public void shouldFailStartCallWithoutDelegate() throws XAException {
        try {
            this.recoveryHelper.start(null, 0);
            fail("IllegalStateException was expected");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Connection has not been opened");
        }
    }

    @Test
    public void shouldDelegateEndCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.end(null, 0);
        verify(this.mockXaResource).end(null, 0);
    }

    @Test
    public void shouldFailEndCallWithoutDelegate() throws XAException {
        try {
            this.recoveryHelper.end(null, 0);
            fail("IllegalStateException was expected");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Connection has not been opened");
        }
    }

    @Test
    public void shouldDelegatePrepareCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.prepare(null);
        verify(this.mockXaResource).prepare(null);
    }

    @Test
    public void shouldFailPrepareCallWithoutDelegate() throws XAException {
        try {
            this.recoveryHelper.prepare(null);
            fail("IllegalStateException was expected");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Connection has not been opened");
        }
    }

    @Test
    public void shouldDelegateCommitCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.commit(null, true);
        verify(this.mockXaResource).commit(null, true);
    }

    @Test
    public void shouldFailCommitCallWithoutDelegate() throws XAException {
        try {
            this.recoveryHelper.commit(null, true);
            fail("IllegalStateException was expected");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Connection has not been opened");
        }
    }

    @Test
    public void shouldDelegateRollbackCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.rollback(null);
        verify(this.mockXaResource).rollback(null);
    }

    @Test
    public void shouldFailRollbackCallWithoutDelegate() throws XAException {
        try {
            this.recoveryHelper.rollback(null);
            fail("IllegalStateException was expected");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Connection has not been opened");
        }
    }

    @Test
    public void shouldDelegateIsSameRMCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.isSameRM(null);
        verify(this.mockXaResource).isSameRM(null);
    }

    @Test
    public void shouldFailIsSameRMCallWithoutDelegate() throws XAException {
        try {
            this.recoveryHelper.isSameRM(null);
            fail("IllegalStateException was expected");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Connection has not been opened");
        }
    }

    @Test
    public void shouldDelegateForgetCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.forget(null);
        verify(this.mockXaResource).forget(null);
    }

    @Test
    public void shouldFailForgetCallWithoutDelegate() throws XAException {
        try {
            this.recoveryHelper.forget(null);
            fail("IllegalStateException was expected");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Connection has not been opened");
        }
    }

    @Test
    public void shouldDelegateGetTransactionTimeoutCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.getTransactionTimeout();
        verify(this.mockXaResource).getTransactionTimeout();
    }

    @Test
    public void shouldFailGetTransactionTimeoutCallWithoutDelegate() throws XAException {
        try {
            this.recoveryHelper.getTransactionTimeout();
            fail("IllegalStateException was expected");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Connection has not been opened");
        }
    }

    @Test
    public void shouldDelegateSetTransactionTimeoutCall() throws XAException {
        this.recoveryHelper.getXAResources();
        this.recoveryHelper.setTransactionTimeout(0);
        verify(this.mockXaResource).setTransactionTimeout(0);
    }

    @Test
    public void shouldFailSetTransactionTimeoutCallWithoutDelegate() throws XAException {
        try {
            this.recoveryHelper.setTransactionTimeout(0);
            fail("IllegalStateException was expected");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage("Connection has not been opened");
        }
    }

}

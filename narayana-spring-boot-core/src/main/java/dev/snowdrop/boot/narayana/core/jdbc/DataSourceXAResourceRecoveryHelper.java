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

import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

/**
 * XAResourceRecoveryHelper implementation which gets XIDs, which needs to be recovered, from the database.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DataSourceXAResourceRecoveryHelper implements XAResourceRecoveryHelper, XAResource {

    private final ConnectionManager connectionManager;
    private final String name;

    /**
     * Create a new {@link DataSourceXAResourceRecoveryHelper} instance.
     *
     * @param xaDataSource the XA data source
     * @param name         the datasource name or {@code null}
     */
    public DataSourceXAResourceRecoveryHelper(XADataSource xaDataSource, String name) {
        this(xaDataSource, null, null, name);
    }

    /**
     * Create a new {@link DataSourceXAResourceRecoveryHelper} instance.
     *
     * @param xaDataSource the XA data source
     * @param user         the database user or {@code null}
     * @param password     the database password or {@code null}
     * @param name         the datasource name or {@code null}
     */
    public DataSourceXAResourceRecoveryHelper(XADataSource xaDataSource, String user, String password, String name) {
        this.connectionManager = new ConnectionManager(xaDataSource, user, password);
        this.name = name;
    }

    @Override
    public boolean initialise(String properties) {
        return true;
    }

    @Override
    public XAResource[] getXAResources() {
        if (!this.connectionManager.isConnected()) {
            try {
                this.connectionManager.connect();
            } catch (XAException ignored) {
                return new XAResource[0];
            }
        }

        return new XAResource[]{new NamedXAResource(this, this.name)};
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        try {
            return this.connectionManager.connectAndApply(delegate -> delegate.recover(flag));
        } finally {
            if (flag == XAResource.TMENDRSCAN) {
                this.connectionManager.disconnect();
            }
        }
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        this.connectionManager.connectAndAccept(delegate -> delegate.start(xid, flags));
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        this.connectionManager.connectAndAccept(delegate -> delegate.end(xid, flags));
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return this.connectionManager.connectAndApply(delegate -> delegate.prepare(xid));
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        this.connectionManager.connectAndAccept(delegate -> delegate.commit(xid, onePhase));
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        this.connectionManager.connectAndAccept(delegate -> delegate.rollback(xid));
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return this.connectionManager.connectAndApply(delegate -> delegate.isSameRM(xaResource));
    }

    @Override
    public void forget(Xid xid) throws XAException {
        this.connectionManager.connectAndAccept(delegate -> delegate.forget(xid));
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return this.connectionManager.connectAndApply(XAResource::getTransactionTimeout);
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return this.connectionManager.connectAndApply(delegate -> delegate.setTransactionTimeout(seconds));
    }
}

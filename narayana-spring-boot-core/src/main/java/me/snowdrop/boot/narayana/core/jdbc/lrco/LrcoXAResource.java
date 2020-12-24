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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.resources.LastResourceCommitOptimisation;

public class LrcoXAResource implements LastResourceCommitOptimisation {

    private final Connection connection;
    private Xid currentXid;

    public LrcoXAResource(Connection connection) {
        this.connection = connection;
    }

    private static XAException convertException(SQLException ex) {
        XAException xa = new XAException(ex.getMessage());
        xa.initCause(ex);
        return xa;
    }

    @Override
    public synchronized void start(Xid xid, int flags) throws XAException {
        switch (flags) {
            case XAResource.TMNOFLAGS:
                if (this.currentXid != null) {
                    throw new XAException("Already enlisted in another transaction with xid " + xid);
                }
                try {
                    this.connection.setAutoCommit(false);
                } catch (SQLException ex) {
                    throw (XAException) new XAException("Count not turn off auto commit for a XA transaction")
                            .initCause(ex);
                }
                this.currentXid = xid;
                break;
            case XAResource.TMRESUME:
                if (!xid.equals(this.currentXid)) {
                    throw new XAException("Attempting to resume in different transaction: expected " + this.currentXid
                            + ", but was " + xid);
                }
                break;
            case XAResource.TMJOIN:
                if (!xid.equals(this.currentXid)) {
                    throw new XAException("Attempting to join in different transaction: expected " + this.currentXid
                            + ", but was " + xid);
                }
                break;
            default:
                throw new XAException("unknown state: " + flags);
        }
    }

    @Override
    public synchronized void end(Xid xid, int flags) throws XAException {
        if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
        }
    }

    @Override
    public synchronized int prepare(Xid xid) throws XAException {
        if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
        }
        return XAResource.XA_OK;
    }

    @Override
    public synchronized void commit(Xid xid, boolean onePhase) throws XAException {
        Objects.requireNonNull(xid, "xid is null");
        if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
        }
        try {
            if (this.connection.isClosed()) {
                throw new XAException("Connection is closed");
            }
            if (!this.connection.isReadOnly()) {
                this.connection.commit();
            }
        } catch (SQLException ex) {
            throw convertException(ex);
        } finally {
            try {
                this.connection.setAutoCommit(true);
            } catch (final SQLException ex) {
                // ignore
            }
            this.currentXid = null;
        }
    }

    @Override
    public synchronized void rollback(Xid xid) throws XAException {
        Objects.requireNonNull(xid, "xid is null");
        if (!this.currentXid.equals(xid)) {
            throw new XAException("Invalid Xid: expected " + this.currentXid + ", but was " + xid);
        }
        try {
            this.connection.rollback();
        } catch (SQLException ex) {
            throw convertException(ex);
        } finally {
            try {
                this.connection.setAutoCommit(true);
            } catch (final SQLException ex) {
                // ignore
            }
            this.currentXid = null;
        }
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        return new Xid[0];
    }

    @Override
    public void forget(Xid xid) throws XAException {
        // Should not be called
        throw new XAException(XAException.XAER_PROTO);
    }

    @Override
    public boolean isSameRM(XAResource xares) throws XAException {
        return this == xares;
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        // Should not be called
        throw new XAException(XAException.XAER_PROTO);
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }
}

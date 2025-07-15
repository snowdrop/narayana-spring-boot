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

package dev.snowdrop.boot.narayana.core.jms.pool;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.LastResource;

public class LastXAResource implements XAResource, LastResource {
    private final XAResource xaResource;

    public LastXAResource(XAResource xaResource) {
        this.xaResource = xaResource;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        this.xaResource.commit(xid, onePhase);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        this.xaResource.end(xid, flags);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        this.xaResource.forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return this.xaResource.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xaRes) throws XAException {
        return this.xaResource.isSameRM(xaRes);
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return this.xaResource.prepare(xid);
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        return this.xaResource.recover(flag);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        this.xaResource.rollback(xid);
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return this.xaResource.setTransactionTimeout(seconds);
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        this.xaResource.start(xid, flags);
    }


}

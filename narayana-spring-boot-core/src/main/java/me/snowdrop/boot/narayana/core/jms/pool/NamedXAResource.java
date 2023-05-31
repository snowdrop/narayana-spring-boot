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

package me.snowdrop.boot.narayana.core.jms.pool;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.XAResourceWrapper;

public class NamedXAResource implements XAResourceWrapper {

    private static final String PRODUCT_NAME = NamedXAResource.class.getPackage().getImplementationTitle();
    private static final String PRODUCT_VERSION = NamedXAResource.class.getPackage().getImplementationVersion();

    private final XAResource xaResource;
    private final String name;

    public NamedXAResource(XAResource xaResource, String name) {
        this.xaResource = xaResource;
        this.name = name;
    }

    @Override
    public XAResource getResource() {
        return this.xaResource;
    }

    @Override
    public String getProductName() {
        return PRODUCT_NAME;
    }

    @Override
    public String getProductVersion() {
        return PRODUCT_VERSION;
    }

    @Override
    public String getJndiName() {
        return this.name;
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        getResource().commit(xid, onePhase);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        getResource().end(xid, flags);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        getResource().forget(xid);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return getResource().getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xaRes) throws XAException {
        if (xaRes instanceof NamedXAResource) {
            return getResource().isSameRM(((NamedXAResource) xaRes).getResource());
        }
        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return getResource().prepare(xid);
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        return getResource().recover(flag);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        getResource().rollback(xid);
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return getResource().setTransactionTimeout(seconds);
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        getResource().start(xid, flags);
    }
}

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

package me.snowdrop.boot.narayana.core.tx;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.XAResourceWrapper;

public class XAResourceWrapperImpl implements XAResourceWrapper {

    private static final String PRODUCT_NAME = XAResourceWrapperImpl.class.getPackage().getImplementationTitle();
    private static final String PRODUCT_VERSION = XAResourceWrapperImpl.class.getPackage().getImplementationVersion();

    private final XAResource delegate;
    private final String name;

    public XAResourceWrapperImpl(XAResource delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public XAResource getResource() {
        return this.delegate;
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
    public int prepare(Xid xid) throws XAException {
        return this.delegate.prepare(xid);
    }

    @Override
    public void start(Xid xid, int flags) throws XAException {
        this.delegate.start(xid, flags);
    }

    @Override
    public void end(Xid xid, int flags) throws XAException {
        this.delegate.end(xid, flags);
    }

    @Override
    public void commit(Xid xid, boolean onePhase) throws XAException {
        this.delegate.commit(xid, onePhase);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        this.delegate.rollback(xid);
    }

    @Override
    public Xid[] recover(int flag) throws XAException {
        return this.delegate.recover(flag);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        this.delegate.forget(xid);
    }

    @Override
    public boolean isSameRM(XAResource xares) throws XAException {
        return this.delegate.isSameRM(xares);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return this.delegate.getTransactionTimeout();
    }

    @Override
    public boolean setTransactionTimeout(int seconds) throws XAException {
        return this.delegate.setTransactionTimeout(seconds);
    }
}

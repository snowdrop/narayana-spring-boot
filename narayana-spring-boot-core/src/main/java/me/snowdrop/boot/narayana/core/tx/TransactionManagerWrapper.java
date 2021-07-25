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

import java.lang.reflect.Proxy;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.resources.LastResourceCommitOptimisation;
import org.jboss.tm.XAResourceWrapper;

public class TransactionManagerWrapper implements TransactionManager {

    private final TransactionManager delegate;
    private final String name;

    public TransactionManagerWrapper(TransactionManager delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    public TransactionManager getDelegate() {
        return this.delegate;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        this.delegate.begin();
    }

    @Override
    public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        this.delegate.commit();
    }

    @Override
    public int getStatus() throws SystemException {
        return this.delegate.getStatus();
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        Transaction tx = this.delegate.getTransaction();
        return tx == null ? null : new TransactionWrapper(this.delegate.getTransaction(), this.name);
    }

    @Override
    public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
        this.delegate.resume(((TransactionWrapper) transaction).transaction);
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        this.delegate.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        this.delegate.setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        this.delegate.setTransactionTimeout(seconds);
    }

    @Override
    public Transaction suspend() throws SystemException {
        return new TransactionWrapper(this.delegate.suspend(), this.name);
    }

    private static final class TransactionWrapper implements Transaction {

        private final Transaction transaction;
        private final String name;

        private TransactionWrapper(Transaction transaction, String name) {
            this.transaction = transaction;
            this.name = name;
        }

        private XAResource getXAResourceWrapper(XAResource xaResource, String name) {
            XAResource wrapper = new XAResourceWrapperImpl(xaResource, name);
            if (xaResource instanceof LastResourceCommitOptimisation) {
                return (XAResource) Proxy.newProxyInstance(
                        wrapper.getClass().getClassLoader(),
                        new Class[]{LastResourceCommitOptimisation.class, XAResourceWrapper.class},
                        (proxy, method, args) -> method.invoke(wrapper, args));
            }
            return wrapper;
        }

        @Override
        public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
            this.transaction.commit();
        }

        @Override
        public boolean delistResource(XAResource xaResource, int flag) throws IllegalStateException, SystemException {
            XAResource wrapper = getXAResourceWrapper(xaResource, this.name);
            return this.transaction.delistResource(wrapper, flag);
        }

        @Override
        public boolean enlistResource(XAResource xaResource) throws IllegalStateException, RollbackException, SystemException {
            XAResource wrapper = getXAResourceWrapper(xaResource, this.name);
            return this.transaction.enlistResource(wrapper);
        }

        @Override
        public int getStatus() throws SystemException {
            return this.transaction.getStatus();
        }

        @Override
        public void registerSynchronization(final Synchronization synchronization) throws IllegalStateException, RollbackException, SystemException {
            this.transaction.registerSynchronization(synchronization);
        }

        @Override
        public void rollback() throws IllegalStateException, SystemException {
            this.transaction.rollback();
        }

        @Override
        public void setRollbackOnly() throws IllegalStateException, SystemException {
            this.transaction.setRollbackOnly();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            TransactionWrapper that = (TransactionWrapper) obj;
            return this.transaction.equals(that.transaction);
        }

        @Override
        public int hashCode() {
            return this.transaction.hashCode();
        }
    }
}

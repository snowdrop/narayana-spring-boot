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

import javax.transaction.xa.XAResource;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.XASession;
import jakarta.transaction.TransactionManager;

import org.messaginghub.pooled.jms.JmsPoolSession;
import org.messaginghub.pooled.jms.pool.PooledXAConnection;

public class PooledNarayanaConnection extends PooledXAConnection {

    private final String name;

    public PooledNarayanaConnection(Connection connection, TransactionManager transactionManager, String name) {
        super(connection, transactionManager);
        this.name = name;
    }

    @Override
    protected XAResource createXaResource(JmsPoolSession session) throws JMSException {
        XAResource xares = ((XASession) session.getInternalSession()).getXAResource();
        if (this.name != null) {
            xares = new NamedXAResource(xares, this.name);
        }
        return xares;
    }

}

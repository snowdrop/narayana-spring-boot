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

import jakarta.jms.Connection;

import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;

public class JmsPoolNarayanaConnectionFactory extends JmsPoolXAConnectionFactory {

    private static final long serialVersionUID = 1709204966732828338L;

    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected PooledNarayanaConnection createPooledConnection(Connection connection) {
        return new PooledNarayanaConnection(connection, getTransactionManager(), getName());
    }
}

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

import jakarta.jms.XAConnectionFactory;

import org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper;

public class NamedJmsXAResourceRecoveryHelper extends JmsXAResourceRecoveryHelper {

    private final String name;

    public NamedJmsXAResourceRecoveryHelper(XAConnectionFactory xaConnectionFactory, String name) {
        this(xaConnectionFactory, null, null, name);
    }

    public NamedJmsXAResourceRecoveryHelper(XAConnectionFactory xaConnectionFactory, String user, String pass, String name) {
        super(xaConnectionFactory, user, pass);
        this.name = name;
    }

    @Override
    public XAResource[] getXAResources() {
        XAResource[] xaResources = super.getXAResources();
        return xaResources.length != 0 ? new XAResource[] { new NamedXAResource(xaResources[0], this.name) } : xaResources;
    }
}

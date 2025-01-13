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

package dev.snowdrop.boot.narayana.core.properties;

import java.sql.Connection;

public class TransactionalDriverProperties {

    private String name = "jdbc";
    private Modifier modifier = Modifier.DEFAULT;
    private IsolationLevel defaultIsolationLevel = IsolationLevel.TRANSACTION_SERIALIZABLE;
    private boolean defaultIsSameRMOverride = false;
    private Pool pool = new Pool();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Modifier getModifier() {
        return this.modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    public IsolationLevel getDefaultIsolationLevel() {
        return this.defaultIsolationLevel;
    }

    public void setDefaultIsolationLevel(IsolationLevel defaultIsolationLevel) {
        this.defaultIsolationLevel = defaultIsolationLevel;
    }

    public boolean isDefaultIsSameRMOverride() {
        return this.defaultIsSameRMOverride;
    }

    public void setDefaultIsSameRMOverride(boolean defaultIsSameRMOverride) {
        this.defaultIsSameRMOverride = defaultIsSameRMOverride;
    }

    public Pool getPool() {
        return this.pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public enum Modifier {
        /**
         * Register {@link com.arjuna.ats.internal.jdbc.drivers.modifiers.IsSameRMModifier} for used JDBC driver.
         */
        IS_SAME_RM,
        /**
         * Register {@link com.arjuna.ats.internal.jdbc.drivers.modifiers.SupportsMultipleConnectionsModifier} for used JDBC driver.
         */
        SUPPORTS_MULTIPLE_CONNECTIONS,
        /**
         * Use default modifier.
         */
        DEFAULT;
    }

    public enum IsolationLevel {

        /**
         * Transaction isolation level TRANSACTION_READ_UNCOMMITTED.
         * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
         */
        TRANSACTION_READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        /**
         * Transaction isolation level TRANSACTION_READ_COMMITTED.
         * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
         */
        TRANSACTION_READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        /**
         * Transaction isolation level TRANSACTION_REPEATABLE_READ.
         * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
         */
        TRANSACTION_REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        /**
         * Transaction isolation level TRANSACTION_SERIALIZABLE.
         * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
         */
        TRANSACTION_SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);

        private final int level;

        IsolationLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return this.level;
        }
    }

    public static class Pool {

        private boolean enabled = false;
        private int maxConnections = 10;

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxConnections() {
            return this.maxConnections;
        }

        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }
    }
}

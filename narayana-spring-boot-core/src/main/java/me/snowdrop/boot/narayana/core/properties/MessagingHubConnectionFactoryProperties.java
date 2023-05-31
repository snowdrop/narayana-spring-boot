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

package me.snowdrop.boot.narayana.core.properties;

import java.time.Duration;

public class MessagingHubConnectionFactoryProperties {

    private boolean enabled;
    private String name;
    private int maxConnections = 1;
    private Duration connectionIdleTimeout = Duration.ofSeconds(30);
    private Duration connectionCheckInterval = Duration.ofMillis(-1);
    private boolean useProviderJMSContext = false;
    private int maxSessionsPerConnection = 500;
    private boolean blockIfSessionPoolIsFull = true;
    private Duration blockIfSessionPoolIsFullTimeout = Duration.ofMillis(-1);
    private boolean useAnonymousProducers = true;

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxConnections() {
        return this.maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Duration getConnectionIdleTimeout() {
        return this.connectionIdleTimeout;
    }

    public void setConnectionIdleTimeout(Duration connectionIdleTimeout) {
        this.connectionIdleTimeout = connectionIdleTimeout;
    }

    public Duration getConnectionCheckInterval() {
        return this.connectionCheckInterval;
    }

    public void setConnectionCheckInterval(Duration connectionCheckInterval) {
        this.connectionCheckInterval = connectionCheckInterval;
    }

    public boolean isUseProviderJMSContext() {
        return this.useProviderJMSContext;
    }

    public void setUseProviderJMSContext(boolean useProviderJMSContext) {
        this.useProviderJMSContext = useProviderJMSContext;
    }

    public int getMaxSessionsPerConnection() {
        return this.maxSessionsPerConnection;
    }

    public void setMaxSessionsPerConnection(int maxSessionsPerConnection) {
        this.maxSessionsPerConnection = maxSessionsPerConnection;
    }

    public boolean isBlockIfSessionPoolIsFull() {
        return this.blockIfSessionPoolIsFull;
    }

    public void setBlockIfSessionPoolIsFull(boolean blockIfSessionPoolIsFull) {
        this.blockIfSessionPoolIsFull = blockIfSessionPoolIsFull;
    }

    public Duration getBlockIfSessionPoolIsFullTimeout() {
        return this.blockIfSessionPoolIsFullTimeout;
    }

    public void setBlockIfSessionPoolIsFullTimeout(Duration blockIfSessionPoolIsFullTimeout) {
        this.blockIfSessionPoolIsFullTimeout = blockIfSessionPoolIsFullTimeout;
    }

    public boolean isUseAnonymousProducers() {
        return this.useAnonymousProducers;
    }

    public void setUseAnonymousProducers(boolean useAnonymousProducers) {
        this.useAnonymousProducers = useAnonymousProducers;
    }
}

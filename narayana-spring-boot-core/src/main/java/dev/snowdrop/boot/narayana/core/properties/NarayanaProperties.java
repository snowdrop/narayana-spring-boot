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

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Subset of Narayana properties which can be configured via Spring configuration. Use
 * jbossts-properties.xml for complete configuration.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@ConfigurationProperties(prefix = NarayanaProperties.PROPERTIES_PREFIX)
public class NarayanaProperties {

    /**
     * Prefix for Narayana specific properties.
     */
    static final String PROPERTIES_PREFIX = "narayana";

    /**
     * Transaction object store directory.
     */
    private String logDir;

    /**
     * Unique node identifier.
     */
    private String nodeIdentifier = "1";

    /**
     * Shorten node identifier if exceed a length of 28 bytes.
     */
    private boolean shortenNodeIdentifierIfNecessary = false;

    /**
     * Enable one phase commit optimization.
     */
    private boolean onePhaseCommit = true;

    /**
     * Transaction timeout in seconds.
     */
    private int defaultTimeout = 60;

    /**
     * Interval in which periodic recovery scans are performed in seconds.
     */
    private int periodicRecoveryPeriod = 120;

    /**
     * Back off period between first and second phases of the recovery scan in seconds.
     */
    private int recoveryBackoffPeriod = 10;

    /**
     * Interval on which the ObjectStore will be scanned for expired items, in hours.
     */
    private int expiryScanInterval = 12;

    /**
     * Database credentials to be used by recovery manager.
     */
    private RecoveryCredentialsProperties recoveryDbCredentials = new RecoveryCredentialsProperties();

    /**
     * JMS credentials to be used by recovery manager.
     */
    private RecoveryCredentialsProperties recoveryJmsCredentials = new RecoveryCredentialsProperties();

    /**
     * Comma-separated list of orphan filters.
     */
    private List<String> xaResourceOrphanFilters = List.of(
            "com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter",
            "com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter",
            "com.arjuna.ats.internal.jta.recovery.arjunacore.JTAActionStatusServiceXAResourceOrphanFilter");

    /**
     * Wrapping plugin to map from XAResourceWrapper to XAResourceRecord.
     */
    private String xaResourceRecordWrappingPlugin = "com.arjuna.ats.internal.jbossatx.jta.XAResourceRecordWrappingPluginImpl";

    /**
     * Interface used for last resource commit optimisation.
     */
    private String lastResourceOptimisationInterface = "org.jboss.tm.LastResource";

    /**
     * JNDI names of CommitMarkableResource instances.
     */
    private List<String> commitMarkableResourceJNDINames = List.of();

    /**
     * Comma-separated list of recovery modules.
     */
    private List<String> recoveryModules = List.of(
            "com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule",
            "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
            "com.arjuna.ats.internal.txoj.recovery.TORecoveryModule",
            "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule");

    /**
     * Comma-separated list of expiry scanners.
     */
    private List<String> expiryScanners = List.of(
            "com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner");

    /**
     * MessagingHub specific properties used if pooled connection factory wrapper is enabled.
     * See <a href="https://github.com/messaginghub/pooled-jms/blob/master/pooled-jms-docs/Configuration.md">...</a> for the list of supported properties.
     */
    @NestedConfigurationProperty
    private final MessagingHubConnectionFactoryProperties messaginghub = new MessagingHubConnectionFactoryProperties();

    /**
     * XA recovery nodes.
     */
    private List<String> xaRecoveryNodes = List.of();

    public String getLogDir() {
        return this.logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    @Deprecated(forRemoval = true)
    public String getTransactionManagerId() {
        return getNodeIdentifier();
    }

    @Deprecated(forRemoval = true)
    public void setTransactionManagerId(String nodeIdentifier) {
        setNodeIdentifier(nodeIdentifier);
    }

    public String getNodeIdentifier() {
        return this.nodeIdentifier;
    }

    public void setNodeIdentifier(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    public boolean isShortenNodeIdentifierIfNecessary() {
        return this.shortenNodeIdentifierIfNecessary;
    }

    public void setShortenNodeIdentifierIfNecessary(boolean shortenNodeIdentifierIfNecessary) {
        this.shortenNodeIdentifierIfNecessary = shortenNodeIdentifierIfNecessary;
    }

    public boolean isOnePhaseCommit() {
        return this.onePhaseCommit;
    }

    public void setOnePhaseCommit(boolean onePhaseCommit) {
        this.onePhaseCommit = onePhaseCommit;
    }

    public int getDefaultTimeout() {
        return this.defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public int getPeriodicRecoveryPeriod() {
        return this.periodicRecoveryPeriod;
    }

    public void setPeriodicRecoveryPeriod(int periodicRecoveryPeriod) {
        this.periodicRecoveryPeriod = periodicRecoveryPeriod;
    }

    public int getRecoveryBackoffPeriod() {
        return this.recoveryBackoffPeriod;
    }

    public void setRecoveryBackoffPeriod(int recoveryBackoffPeriod) {
        this.recoveryBackoffPeriod = recoveryBackoffPeriod;
    }

    public int getExpiryScanInterval() {
        return this.expiryScanInterval;
    }

    public void setExpiryScanInterval(int expiryScanInterval) {
        this.expiryScanInterval = expiryScanInterval;
    }

    public List<String> getXaResourceOrphanFilters() {
        return this.xaResourceOrphanFilters;
    }

    public void setXaResourceOrphanFilters(List<String> xaResourceOrphanFilters) {
        this.xaResourceOrphanFilters = xaResourceOrphanFilters;
    }

    public String getXaResourceRecordWrappingPlugin() {
        return this.xaResourceRecordWrappingPlugin;
    }

    public void setXaResourceRecordWrappingPlugin(String xaResourceRecordWrappingPlugin) {
        this.xaResourceRecordWrappingPlugin = xaResourceRecordWrappingPlugin;
    }

    public String getLastResourceOptimisationInterface() {
        return this.lastResourceOptimisationInterface;
    }

    public void setLastResourceOptimisationInterface(String lastResourceOptimisationInterface) {
        this.lastResourceOptimisationInterface = lastResourceOptimisationInterface;
    }

    public List<String> getCommitMarkableResourceJNDINames() {
        return this.commitMarkableResourceJNDINames;
    }

    public void setCommitMarkableResourceJNDINames(List<String> commitMarkableResourceJNDINames) {
        this.commitMarkableResourceJNDINames = commitMarkableResourceJNDINames;
    }

    public List<String> getRecoveryModules() {
        return this.recoveryModules;
    }

    public void setRecoveryModules(List<String> recoveryModules) {
        this.recoveryModules = recoveryModules;
    }

    public List<String> getExpiryScanners() {
        return this.expiryScanners;
    }

    public void setExpiryScanners(List<String> expiryScanners) {
        this.expiryScanners = expiryScanners;
    }

    public RecoveryCredentialsProperties getRecoveryDbCredentials() {
        return this.recoveryDbCredentials;
    }

    public void setRecoveryDbCredentials(RecoveryCredentialsProperties recoveryDbCredentials) {
        this.recoveryDbCredentials = recoveryDbCredentials;
    }

    public RecoveryCredentialsProperties getRecoveryJmsCredentials() {
        return this.recoveryJmsCredentials;
    }

    public void setRecoveryJmsCredentials(RecoveryCredentialsProperties recoveryJmsCredentials) {
        this.recoveryJmsCredentials = recoveryJmsCredentials;
    }

    public MessagingHubConnectionFactoryProperties getMessaginghub() {
        return this.messaginghub;
    }

    public List<String> getXaRecoveryNodes() {
        return this.xaRecoveryNodes;
    }

    public void setXaRecoveryNodes(List<String> xaRecoveryNodes) {
        this.xaRecoveryNodes = xaRecoveryNodes;
    }
}

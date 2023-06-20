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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
     * Unique transaction manager id.
     */
    private String transactionManagerId = "1";

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
    private List<String> xaResourceOrphanFilters = new ArrayList<>(Arrays.asList(
            "com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter",
            "com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter",
            "com.arjuna.ats.internal.jta.recovery.arjunacore.JTAActionStatusServiceXAResourceOrphanFilter"));

    /**
     * Wrapping plugin to map from XAResourceWrapper to XAResourceRecord.
     */
    private String xaResourceRecordWrappingPlugin = "com.arjuna.ats.internal.jbossatx.jta.XAResourceRecordWrappingPluginImpl";

    /**
     * Comma-separated list of recovery modules.
     */
    private List<String> recoveryModules = new ArrayList<>(Arrays.asList(
            "com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule",
            "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
            "com.arjuna.ats.internal.txoj.recovery.TORecoveryModule",
            "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule"));

    /**
     * Comma-separated list of expiry scanners.
     */
    private List<String> expiryScanners = new ArrayList<>(Collections.singletonList(
            "com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner"));

    /**
     * MessagingHub specific properties used if pooled connection factory wrapper is enabled.
     * See https://github.com/messaginghub/pooled-jms/blob/master/pooled-jms-docs/Configuration.md for the list of supported properties.
     */
    @NestedConfigurationProperty
    private final MessagingHubConnectionFactoryProperties messaginghub = new MessagingHubConnectionFactoryProperties();

    /**
     * XA recovery nodes.
     */
    private List<String> xaRecoveryNodes = new ArrayList<>(Arrays.asList("1"));

    public String getLogDir() {
        return this.logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public String getTransactionManagerId() {
        return this.transactionManagerId;
    }

    public void setTransactionManagerId(String transactionManagerId) {
        this.transactionManagerId = transactionManagerId;
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

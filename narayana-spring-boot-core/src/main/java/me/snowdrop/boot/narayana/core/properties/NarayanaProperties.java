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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Map of DBCP specific properties used if pooled data source wrapper is enabled.
     * See https://commons.apache.org/proper/commons-dbcp/configuration.html for the list of supported properties.
     */
    private Map<String, String> dbcp = new HashMap<>();

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

    public void setDefaultTimeout(int defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public List<String> getXaResourceOrphanFilters() {
        return this.xaResourceOrphanFilters;
    }

    public void setXaResourceOrphanFilters(List<String> xaResourceOrphanFilters) {
        this.xaResourceOrphanFilters = xaResourceOrphanFilters;
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

    @Deprecated
    public String getRecoveryDbUser() {
        return this.recoveryDbCredentials.getUser();
    }

    @Deprecated
    public void setRecoveryDbUser(String recoveryDbUser) {
        this.recoveryDbCredentials.setUser(recoveryDbUser);
    }

    @Deprecated
    public String getRecoveryDbPass() {
        return this.recoveryDbCredentials.getPassword();
    }

    @Deprecated
    public void setRecoveryDbPass(String recoveryDbPass) {
        this.recoveryDbCredentials.setPassword(recoveryDbPass);
    }

    public RecoveryCredentialsProperties getRecoveryJmsCredentials() {
        return this.recoveryJmsCredentials;
    }

    public void setRecoveryJmsCredentials(RecoveryCredentialsProperties recoveryJmsCredentials) {
        this.recoveryJmsCredentials = recoveryJmsCredentials;
    }

    @Deprecated
    public String getRecoveryJmsUser() {
        return this.recoveryJmsCredentials.getUser();
    }

    @Deprecated
    public void setRecoveryJmsUser(String recoveryJmsUser) {
        this.recoveryJmsCredentials.setUser(recoveryJmsUser);
    }

    @Deprecated
    public String getRecoveryJmsPass() {
        return this.recoveryJmsCredentials.getUser();
    }

    @Deprecated
    public void setRecoveryJmsPass(String recoveryJmsPass) {
        this.recoveryJmsCredentials.setPassword(recoveryJmsPass);
    }

    public Map<String, String> getDbcp() {
        return this.dbcp;
    }

    public void setDbcp(Map<String, String> dbcp) {
        this.dbcp = dbcp;
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

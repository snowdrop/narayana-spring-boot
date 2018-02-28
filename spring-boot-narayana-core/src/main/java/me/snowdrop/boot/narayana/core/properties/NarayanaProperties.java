/*
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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

/**
 * Subset of Narayana properties which can be configured via Spring configuration. Use
 * jbossts-properties.xml for complete configuration.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class NarayanaProperties {

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
     * Database username to be used by recovery manager.
     */
    private String recoveryDbUser = null;

    /**
     * Database password to be used by recovery manager.
     */
    private String recoveryDbPass = null;

    /**
     * JMS username to be used by recovery manager.
     */
    private String recoveryJmsUser = null;

    /**
     * JMS password to be used by recovery manager.
     */
    private String recoveryJmsPass = null;

    /**
     * Comma-separated list of orphan filters.
     */
    private List<String> xaResourceOrphanFilters = new ArrayList<>(Arrays.asList(
            "com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter",
            "com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter"));

    /**
     * Comma-separated list of recovery modules.
     */
    private List<String> recoveryModules = new ArrayList<>(Arrays.asList(
            "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
            "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule"));

    /**
     * Comma-separated list of expiry scanners.
     */
    private List<String> expiryScanners = new ArrayList<>(Collections.singletonList(
            "com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner"));

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

    public String getRecoveryDbUser() {
        return this.recoveryDbUser;
    }

    public void setRecoveryDbUser(String recoveryDbUser) {
        this.recoveryDbUser = recoveryDbUser;
    }

    public String getRecoveryDbPass() {
        return this.recoveryDbPass;
    }

    public void setRecoveryDbPass(String recoveryDbPass) {
        this.recoveryDbPass = recoveryDbPass;
    }

    public String getRecoveryJmsUser() {
        return this.recoveryJmsUser;
    }

    public void setRecoveryJmsUser(String recoveryJmsUser) {
        this.recoveryJmsUser = recoveryJmsUser;
    }

    public String getRecoveryJmsPass() {
        return this.recoveryJmsPass;
    }

    public void setRecoveryJmsPass(String recoveryJmsPass) {
        this.recoveryJmsPass = recoveryJmsPass;
    }

}

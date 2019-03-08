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

package me.snowdrop.boot.narayana.openshift.recovery;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Recovery controller spring-boot configuration.
 *
 * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
 */
@ConfigurationProperties(prefix = "snowdrop.narayana.openshift.recovery")
public class StatefulsetRecoveryControllerProperties {

    /**
     * Enables the recovery controller.
     * The recovery controller runs only on pod 0 of the Statefulset by default.
     */
    private boolean enabled = false;

    /**
     * Enables the recovery controller on all pods, not only on pod 0 of the Statefulset.
     */
    private boolean enabledOnAllPods = false;

    /**
     * The delay in milliseconds between two runs of the recovery controller.
     */
    private long period = 30000;

    /**
     * The target statefulset to monitor.
     */
    private String statefulset;

    /**
     * The name of the current pod, to be filled using Kubernetes downward API.
     */
    private String currentPodName;

    /**
     * Path of the pod directory where pods will save their status.
     */
    private String statusDir;

    /**
     * Enables log-scraping based error detection during recovery.
     */
    private boolean logScrapingErrorDetectionEnabled = false;

    /**
     * Configures the pattern used during log-scraping to detect errors.
     */
    private String logScrapingErrorDetectionPattern = "WARN|ERROR";

    public long getPeriod() {
        return this.period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public String getStatefulset() {
        return this.statefulset;
    }

    public void setStatefulset(String statefulset) {
        this.statefulset = statefulset;
    }

    public String getCurrentPodName() {
        return this.currentPodName;
    }

    public void setCurrentPodName(String currentPodName) {
        this.currentPodName = currentPodName;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabledOnAllPods() {
        return this.enabledOnAllPods;
    }

    public void setEnabledOnAllPods(boolean enabledOnAllPods) {
        this.enabledOnAllPods = enabledOnAllPods;
    }

    public String getStatusDir() {
        return this.statusDir;
    }

    public void setStatusDir(String statusDir) {
        this.statusDir = statusDir;
    }

    public boolean isLogScrapingErrorDetectionEnabled() {
        return this.logScrapingErrorDetectionEnabled;
    }

    public void setLogScrapingErrorDetectionEnabled(boolean logScrapingErrorDetectionEnabled) {
        this.logScrapingErrorDetectionEnabled = logScrapingErrorDetectionEnabled;
    }

    public String getLogScrapingErrorDetectionPattern() {
        return this.logScrapingErrorDetectionPattern;
    }

    public void setLogScrapingErrorDetectionPattern(String logScrapingErrorDetectionPattern) {
        this.logScrapingErrorDetectionPattern = logScrapingErrorDetectionPattern;
    }
}

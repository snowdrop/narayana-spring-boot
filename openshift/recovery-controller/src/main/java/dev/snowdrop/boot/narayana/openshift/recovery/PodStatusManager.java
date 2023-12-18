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

package dev.snowdrop.boot.narayana.openshift.recovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the termination status of a pod.
 *
 * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
 */
public class PodStatusManager {

    private static final Logger LOG = LoggerFactory.getLogger(PodStatusManager.class);

    private StatefulsetRecoveryControllerProperties properties;

    public PodStatusManager(StatefulsetRecoveryControllerProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties not set");
        Objects.requireNonNull(properties.getStatusDir(), "statusDir has not been provided in recovery controller configuration");
        Objects.requireNonNull(properties.getStatefulset(), "statefulset property missing in recovery controller configuration");
        Objects.requireNonNull(properties.getCurrentPodName(), "current-pod-name property missing in recovery controller configuration");
    }

    public void setStatus(PodStatus status) {
        this.setStatus(this.properties.getCurrentPodName(), status);
    }

    public Optional<PodStatus> getStatus() {
        return this.getStatus(this.properties.getCurrentPodName());
    }

    public Map<String, Optional<PodStatus>> getAllPodsStatus() {
        Map<String, Optional<PodStatus>> podStatuses = new TreeMap<>();

        File baseDir = getBaseDir();
        String[] logFiles = baseDir.list((f, name) -> name.startsWith(this.properties.getStatefulset()) && !new File(f, name).isDirectory());
        if (logFiles == null) {
            logFiles = new String[0];
        }
        for (String logFile : logFiles) {
            Optional<PodStatus> status = getStatus(logFile);
            podStatuses.put(logFile, status);
        }

        return podStatuses;
    }

    void setStatus(String pod, PodStatus status) {
        File baseDir = getBaseDir();
        File podFile = new File(baseDir, pod);
        try (FileWriter out = new FileWriter(podFile)) {
            out.write(status.name());
        } catch (IOException ex) {
            throw new RuntimeException("Cannot write status in pod file " + podFile.getAbsolutePath(), ex);
        }
    }

    Optional<PodStatus> getStatus(String pod) {
        File file = new File(getBaseDir(), pod);
        try {
            if (!file.exists()) {
                LOG.warn("Cannot find file {} for getting the status", file.getAbsolutePath());
                return Optional.empty();
            }

            String statusLine;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                statusLine = reader.readLine();
            }
            if (statusLine != null) {
                PodStatus status = PodStatus.valueOf(statusLine
                        .replace("\r", "")
                        .replace("\n", "")
                        .trim());
                return Optional.of(status);
            }
            LOG.warn("Cannot find data in file {}", file.getAbsolutePath());
            return Optional.empty();
        } catch (Exception ex) {
            LOG.warn("Exception while reading file " + file.getAbsolutePath() + " for getting the status", ex);
            return Optional.empty();
        }
    }

    private File getBaseDir() {
        File baseDir = new File(this.properties.getStatusDir());
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new RuntimeException("Cannot create status dir in " + this.properties.getStatusDir());
        }
        return baseDir;
    }


}

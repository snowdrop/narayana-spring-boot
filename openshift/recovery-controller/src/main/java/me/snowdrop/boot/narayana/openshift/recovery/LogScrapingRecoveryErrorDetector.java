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

package me.snowdrop.boot.narayana.openshift.recovery;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects errors in recovery scan by scraping the logs.
 *
 * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
 */
public class LogScrapingRecoveryErrorDetector implements RecoveryErrorDetector {

    private static Logger LOG = LoggerFactory.getLogger(LogScrapingRecoveryErrorDetector.class);

    private static final String START_MESSAGE = "LOG-SCRAPING-START";
    private static final String STOP_MESSAGE = "LOG-SCRAPING-STOP";

    private String podName;

    private Predicate<String> matcher;

    private KubernetesClient kubernetesClient;

    private LogWatch logWatch;

    private boolean watchClosed;

    private boolean errorMessageFound;

    private boolean startMessageFound;

    private volatile boolean stopMessageFound;

    private ExecutorService executorService;

    public LogScrapingRecoveryErrorDetector(String podName, String pattern, KubernetesClient kubernetesClient) {
        this.podName = Objects.requireNonNull(podName, "pod name cannot be null");
        this.matcher = Pattern.compile(pattern).asPredicate();
        this.kubernetesClient = Objects.requireNonNull(kubernetesClient, "kubernetes client cannot be null");
    }

    @Override
    public void startDetection() {
        if (this.logWatch == null && this.executorService == null) {
            // Printing the START_MESSAGE to limit log scraping
            LOG.info("Log-scraping recovery error detector started: {}", START_MESSAGE);
            this.watchClosed = false;
            this.errorMessageFound = false;
            this.startMessageFound = false;
            this.stopMessageFound = false;

            this.logWatch = this.kubernetesClient.pods().withName(this.podName).watchLog();

            this.executorService = Executors.newSingleThreadExecutor();
            this.startLogScraping();
        }
    }

    protected void startLogScraping() {
        this.executorService.execute(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.logWatch.getOutput()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(START_MESSAGE)) {
                        this.startMessageFound = true;
                        continue;
                    }
                    if (line.contains(STOP_MESSAGE)) {
                        this.stopMessageFound = true;
                        break;
                    }

                    if (this.startMessageFound && !this.stopMessageFound && this.matcher.test(line)) {
                        this.errorMessageFound = true;
                        LOG.info("Found problem during log scraping");
                    }
                }
            } catch (Exception ex) {
                if (!this.watchClosed) {
                    throw new RuntimeException("Problem while watching the pod logs", ex);
                }
            } finally {
                if (!this.startMessageFound) {
                    LOG.info("Start message not found in log");
                }
                if (!this.stopMessageFound) {
                    LOG.info("Stop message not found in log");
                }
            }
        });
    }

    @Override
    public void stopDetection() {
        // Printing the STOP_MESSAGE to limit log scraping
        LOG.info("Log-scraping recovery error detector stopped: {}", STOP_MESSAGE);

        waitForStopMessage();

        if (this.logWatch != null) {
            try {
                this.watchClosed = true;
                this.logWatch.close();
            } catch (Exception ex) {
                LOG.info("Problem while closing the log watch", ex);
            } finally {
                this.logWatch = null;
            }
        }
        if (this.executorService != null) {
            try {
                this.executorService.shutdown();
                if (!this.executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    this.executorService.shutdownNow();
                    if (!this.executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                        LOG.info("Log scraping executor service did not terminate within the timeframe");
                    }
                }
            } catch (Exception ex) {
                LOG.info("Problem while closing the executor service", ex);
            } finally {
                this.executorService = null;
            }
        }
    }

    @Override
    public boolean errorsDetected() {
        return this.errorMessageFound || !this.startMessageFound || !this.stopMessageFound;
    }


    private void waitForStopMessage() {
        try {
            int attempts = 10;
            for (int i = 0; i < attempts && !this.stopMessageFound; i++) {
                LOG.debug("Waiting for stop message");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } finally {
            if (!this.stopMessageFound) {
                LOG.info("Problem during log scraping: stop message not reached");
            }
        }
    }

}

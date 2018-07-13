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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel Narayana termination controller.
 *
 * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
 */
public class NarayanaRecoveryTerminationController {

    private static final Logger LOG = LoggerFactory.getLogger(NarayanaRecoveryTerminationController.class);

    private PodStatusManager podStatusManager;

    private List<ServiceShutdownController> shutdownHooks;

    private List<RecoveryErrorDetector> recoveryErrorDetectors;

    public NarayanaRecoveryTerminationController(PodStatusManager podStatusManager, List<ServiceShutdownController> shutdownControllers, List<RecoveryErrorDetector> recoveryErrorDetectors) {
        this.podStatusManager = Objects.requireNonNull(podStatusManager, "podStatusManager cannot be null");
        this.shutdownHooks = Objects.requireNonNull(shutdownControllers, "shutdownControllers cannot be null");
        this.recoveryErrorDetectors = Objects.requireNonNull(recoveryErrorDetectors, "recoveryErrorDetectors cannot be null");
    }

    public void start() {
        LOG.info("Narayana recovery termination controller started");
        this.podStatusManager.setStatus(PodStatus.RUNNING);
    }

    public void stop() {
        try {
            // Stop all services that may use transactions
            waitForShutdownControllersToStop();

            // Start error detectors
            startRecoveryErrorDetectors();

            LOG.info("Performing transaction recovery scan...");
            RecoveryManager.manager().scan();
            LOG.info("Performing second run of transaction recovery scan...");
            RecoveryManager.manager().scan();

        } catch (Exception ex) {
            LOG.error("Error while performing transaction scan", ex);
        } finally {
            stopRecoveryErrorDetectors();
        }

        if (recoveryErrorsDetected()) {
            LOG.error("Errors detected while performing the recovery manager scan. Scan result is invalid.");
            this.podStatusManager.setStatus(PodStatus.PENDING);
        } else {
            try {
                List<Uid> pendingUids = getPendingUids();
                if (pendingUids.isEmpty()) {
                    LOG.info("There are no pending transactions left");
                    this.podStatusManager.setStatus(PodStatus.STOPPED);
                } else {
                    LOG.warn("There are pending transactions: {}", pendingUids);
                    this.podStatusManager.setStatus(PodStatus.PENDING);
                }

            } catch (Exception ex) {
                LOG.error("Error while trying to detect pending transactions", ex);
            }
        }

    }

    private void waitForShutdownControllersToStop() throws InterruptedException {
        for (ServiceShutdownController hook : this.shutdownHooks) {
            hook.stop();
        }
        LOG.info("All service shutdown hooks stopped");
    }

    private List<Uid> getPendingUids() throws Exception {
        InputObjectState types = new InputObjectState();
        StoreManager.getRecoveryStore().allTypes(types);

        List<Uid> allUIDs = new ArrayList<>();
        for (String typeName = types.unpackString(); typeName != null && typeName.compareTo("") != 0; typeName = types.unpackString()) {
            List<Uid> uids = getPendingUids(typeName);

            if (uids.isEmpty()) {
                LOG.debug("Found {} UIDs for action type {}", 0, typeName);
            } else {
                LOG.warn("Found {} UIDs for action type {}", uids.size(), typeName);
            }
            allUIDs.addAll(uids);
        }

        return allUIDs;
    }

    private List<Uid> getPendingUids(String type) throws Exception {
        List<Uid> uidList = new ArrayList<>();
        InputObjectState uids = new InputObjectState();
        if (!StoreManager.getRecoveryStore().allObjUids(type, uids)) {
            throw new RuntimeException("Cannot obtain pending Uids");
        }

        if (uids.notempty()) {
            Uid u;
            do {
                u = UidHelper.unpackFrom(uids);

                if (Uid.nullUid().notEquals(u)) {
                    uidList.add(u);
                }
            } while (Uid.nullUid().notEquals(u));
        }

        return uidList;
    }

    private void startRecoveryErrorDetectors() {
        for (RecoveryErrorDetector recoveryErrorDetector : this.recoveryErrorDetectors) {
            recoveryErrorDetector.startDetection();
        }
    }

    private void stopRecoveryErrorDetectors() {
        for (RecoveryErrorDetector recoveryErrorDetector : this.recoveryErrorDetectors) {
            recoveryErrorDetector.stopDetection();
        }
    }

    private boolean recoveryErrorsDetected() {
        for (RecoveryErrorDetector recoveryErrorDetector : this.recoveryErrorDetectors) {
            if (recoveryErrorDetector.errorsDetected()) {
                return true;
            }
        }
        return false;
    }

}

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

import com.arjuna.ats.jta.logging.jtaLogger;

/**
 * Narayana internal recovery error detector which has been introduced in JBTM-3017.
 *
 * @author <a href="mailto:zfeng@redhat.com>Zheng Feng</a>
 */
public class NarayanaInternalRecoveryErrorDetector implements RecoveryErrorDetector {
    @Override
    public void startDetection() {

    }

    @Override
    public void stopDetection() {

    }

    @Override
    public boolean errorsDetected() {
        return jtaLogger.isRecoveryProblems();
    }
}

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

package me.snowdrop.boot.narayana.utils;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class BytemanHelper {

    private static final Logger logger = LoggerFactory.getLogger(BytemanHelper.class);
    private static int commitsCounter;

    public static void reset() {
        commitsCounter = 0;
    }

    public void failFirstCommit(Uid uid) {
        // Increment is called first, so counter should be 1
        if (commitsCounter == 1) {
            logger.info(BytemanHelper.class.getName() + " fail first commit");
            ActionManager.manager().remove(uid);
            ThreadActionData.popAction();
            throw new RuntimeException("Failing first commit");
        }
    }

    public void incrementCommitsCounter() {
        commitsCounter++;
        logger.info(BytemanHelper.class.getName() + " increment commits counter: " + commitsCounter);
    }

}

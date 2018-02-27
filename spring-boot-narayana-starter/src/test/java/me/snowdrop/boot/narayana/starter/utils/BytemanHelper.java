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

package me.snowdrop.boot.narayana.starter.utils;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionManager;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class BytemanHelper {

    private static int commitsCounter;

    public static void reset() {
        commitsCounter = 0;
    }

    public void failFirstCommit(Uid uid) {
        // Increment is called first, so counter should be 1
        if (commitsCounter == 1) {
            System.out.println(BytemanHelper.class.getName() + " fail first commit");
            ActionManager.manager().remove(uid);
            ThreadActionData.popAction();
            throw new RuntimeException("Failing first commit");
        }
    }

    public void incrementCommitsCounter() {
        commitsCounter++;
        System.out.println(BytemanHelper.class.getName() + " increment commits counter: " + commitsCounter);
    }

}

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

/**
 * Enumerates the high level states of a pod.
 *
 * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
 */
public enum PodStatus {

    /**
     * The pod has been marked as RUNNING.
     */
    RUNNING,

    /**
     * The pod has terminated, but there's PENDING work to be done.
     */
    PENDING,

    /**
     * The pod has been gracefully STOPPED.
     */
    STOPPED
}

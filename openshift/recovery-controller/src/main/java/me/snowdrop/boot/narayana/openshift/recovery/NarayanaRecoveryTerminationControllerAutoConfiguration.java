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

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Camel Narayana controller auto configuration.
 *
 * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
 */
@Configuration
@AutoConfigureAfter(name = {
        "me.snowdrop.boot.narayana.openshift.recovery.StatefulsetRecoveryControllerAutoConfiguration",
        "me.snowdrop.boot.narayana.SpringBoot1NarayanaConfiguration",
        "me.snowdrop.boot.narayana.SpringBoot2NarayanaConfiguration"
})
@ConditionalOnBean({PodStatusManager.class})
public class NarayanaRecoveryTerminationControllerAutoConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @DependsOn("recoveryManagerService")
    @ConditionalOnMissingBean(NarayanaRecoveryTerminationController.class)
    public NarayanaRecoveryTerminationController narayanaRecoveryTerminationController(PodStatusManager podStatusManager, List<ServiceShutdownController> shutdownControllers) {
        return new NarayanaRecoveryTerminationController(podStatusManager, shutdownControllers);
    }

}

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

import java.util.List;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

/**
 * Camel Narayana controller auto configuration.
 *
 * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
 */
@AutoConfiguration(after = StatefulsetRecoveryControllerAutoConfiguration.class)
@ConditionalOnBean({PodStatusManager.class})
public class NarayanaRecoveryTerminationControllerAutoConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @DependsOn("recoveryManagerService")
    @ConditionalOnMissingBean(NarayanaRecoveryTerminationController.class)
    public NarayanaRecoveryTerminationController narayanaRecoveryTerminationController(PodStatusManager podStatusManager, ObjectProvider<List<ServiceShutdownController>> shutdownControllers, ObjectProvider<List<RecoveryErrorDetector>> recoveryErrorDetectors) {
        return new NarayanaRecoveryTerminationController(podStatusManager, shutdownControllers.getIfAvailable(List::of), recoveryErrorDetectors.getIfAvailable(List::of));
    }

    @Bean
    @ConditionalOnProperty(value = "snowdrop.narayana.openshift.recovery.log-scraping-error-detection-enabled", matchIfMissing = true)
    @ConditionalOnMissingBean(LogScrapingRecoveryErrorDetector.class)
    public LogScrapingRecoveryErrorDetector logScrapingRecoveryErrorDetector(StatefulsetRecoveryControllerProperties properties, KubernetesClient kubernetesClient) {
        return new LogScrapingRecoveryErrorDetector(properties.getCurrentPodName(), properties.getLogScrapingErrorDetectionPattern(), kubernetesClient);
    }

}

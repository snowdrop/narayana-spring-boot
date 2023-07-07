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

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Recovery controller spring-boot auto configuration.
 *
 * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(StatefulsetRecoveryControllerProperties.class)
@ConditionalOnProperty("snowdrop.narayana.openshift.recovery.enabled")
public class StatefulsetRecoveryControllerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PodStatusManager.class)
    public PodStatusManager podStatusManager(StatefulsetRecoveryControllerProperties properties) {
        return new PodStatusManager(properties);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(KubernetesClient.class)
    public KubernetesClient kubernetesClient() {
        return new DefaultOpenShiftClient();
    }

    @Bean
    @ConditionalOnMissingBean(StatefulsetRecoveryController.class)
    public StatefulsetRecoveryController statefulsetRecoveryController(StatefulsetRecoveryControllerProperties properties, PodStatusManager podStatusManager, KubernetesClient kubernetesClient) {
        return new StatefulsetRecoveryController(properties, podStatusManager, kubernetesClient);
    }

}

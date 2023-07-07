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

import java.io.File;
import java.net.HttpURLConnection;
import java.time.Duration;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import me.snowdrop.boot.narayana.openshift.recovery.camel.CamelServiceShutdownControllerAutoConfiguration.CamelServiceShutdownController;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootApplication
class RecoveryControllerIT {

    private static KubernetesMockServer kubernetesServer;
    private static KubernetesClient kubernetesClient;

    private static final Pod READY;
    private static final PodList READY_LIST;

    static {
        READY = new PodBuilder()
                .withNewMetadata()
                .withResourceVersion("1")
                .withName("pod-0")
                .withNamespace("test")
                .endMetadata()
                .withNewStatus()
                .addNewCondition()
                .withType("Ready")
                .withStatus("True")
                .endCondition()
                .endStatus()
                .build();
        READY_LIST = new PodListBuilder()
                .withNewMetadata()
                .withResourceVersion("1")
                .endMetadata()
                .withItems(READY)
                .build();
    }

    @BeforeAll
    static void beforeAll() {
        kubernetesServer = new KubernetesMockServer(false);
        kubernetesServer.init();
        kubernetesClient = kubernetesServer.createClient();
    }

    @AfterAll
    static void afterAll() {
        kubernetesClient.close();
        kubernetesServer.destroy();
    }

    @AfterEach
    void afterEach() {
        kubernetesServer.reset();
        kubernetesClient = kubernetesServer.createClient();
    }

    @Test
    void successfulRoundtripTest() throws Exception {
        kubernetesServer.expect()
                .get()
                .withPath("/api/v1/namespaces/test/pods?fieldSelector=metadata.name%3Dpod-0")
                .andReturn(HttpURLConnection.HTTP_OK, READY_LIST)
                .always();
        kubernetesServer.expect()
                .get()
                .withPath("/api/v1/namespaces/test/pods/pod-0")
                .andReturn(HttpURLConnection.HTTP_OK, READY)
                .always();
        kubernetesServer.expect().withPath("/api/v1/namespaces/test/pods/pod-0/log?pretty=false&follow=true")
                .andReturnChunked(HttpURLConnection.HTTP_OK,
                        "LOG-SCRAPING-START",
                        System.lineSeparator(),
                        "LOG-SCRAPING-STOP",
                        System.lineSeparator())
                .once();

        ConfigurableApplicationContext applicationContext = SpringApplication.run(RecoveryControllerIT.class, new String[0]);
        assertThat(new File("target/status/pod-0")).content().isEqualTo("RUNNING");
        ServiceShutdownController ssc = applicationContext.getBean(ServiceShutdownController.class);
        assertThat(ssc).isInstanceOf(CamelServiceShutdownController.class);
        RecoveryErrorDetector red = applicationContext.getBean(RecoveryErrorDetector.class);
        assertThat(red).isInstanceOf(LogScrapingRecoveryErrorDetector.class);
        assertThat(applicationContext.getBean(PodStatusManager.class)).isNotNull();
        assertThat(applicationContext.getBean(NarayanaRecoveryTerminationController.class)).isNotNull();
        assertThat(applicationContext.getBean(StatefulsetRecoveryController.class)).isNotNull();
        await("Let StatefulsetRecoveryController work a bit").pollDelay(Duration.ofSeconds(5)).until(() -> true);
        applicationContext.close();
        assertThat(new File("target/status/pod-0")).content().isEqualTo("STOPPED");
    }

    @Test
    void unsuccessfulRoundtripTest() throws Exception {
        kubernetesServer.expect()
                .get()
                .withPath("/api/v1/namespaces/test/pods?fieldSelector=metadata.name%3Dpod-0")
                .andReturn(HttpURLConnection.HTTP_OK, READY_LIST)
                .always();
        kubernetesServer.expect()
                .get()
                .withPath("/api/v1/namespaces/test/pods/pod-0")
                .andReturn(HttpURLConnection.HTTP_OK, READY)
                .always();
        kubernetesServer.expect().withPath("/api/v1/namespaces/test/pods/pod-0/log?pretty=false&follow=true")
                .andReturnChunked(HttpURLConnection.HTTP_OK,
                        "LOG-SCRAPING-START",
                        System.lineSeparator(),
                        "WARN",
                        System.lineSeparator(),
                        "LOG-SCRAPING-STOP",
                        System.lineSeparator())
                .once();

        ConfigurableApplicationContext applicationContext = SpringApplication.run(RecoveryControllerIT.class, new String[0]);
        assertThat(new File("target/status/pod-0")).content().isEqualTo("RUNNING");
        await("Let StatefulsetRecoveryController work a bit").pollDelay(Duration.ofSeconds(5)).until(() -> true);
        applicationContext.close();
        assertThat(new File("target/status/pod-0")).content().isEqualTo("PENDING");
    }

    @TestConfiguration
    public static class SpringConfig {

        @Bean
        public KubernetesClient kubernetesClient() {
            return RecoveryControllerIT.kubernetesClient;
        }
    }
}

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

package me.snowdrop.boot.narayana.openshift.recovery.camel;

import me.snowdrop.boot.narayana.openshift.recovery.NarayanaRecoveryTerminationControllerAutoConfiguration;
import me.snowdrop.boot.narayana.openshift.recovery.ServiceShutdownController;
import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shutdown controller auto-configuration for the Camel context.
 *
 * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
 */
@Configuration
@AutoConfigureAfter({CamelAutoConfiguration.class})
@AutoConfigureBefore({NarayanaRecoveryTerminationControllerAutoConfiguration.class})
public class CamelServiceShutdownControllerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CamelServiceShutdownController.class)
    public CamelServiceShutdownController camelShutdownHook(CamelContext camelContext) {
        return new CamelServiceShutdownController(camelContext);
    }

    /**
     * Waits for the Camel context to stop.
     *
     * @author <a href="mailto:nferraro@redhat.com">Nicola Ferraro</a>
     */
    public static class CamelServiceShutdownController implements ServiceShutdownController {

        private static final Logger LOG = LoggerFactory.getLogger(CamelServiceShutdownController.class);

        private CamelContext camelContext;

        public CamelServiceShutdownController(CamelContext camelContext) {
            this.camelContext = camelContext;
        }

        @Override
        public void stop() throws InterruptedException {
            LOG.info("Waiting for Camel context to stop...");
            int attempts = 200;
            boolean forcedShutdown = false;
            ServiceStatus camelStatus = this.camelContext.getStatus();
            for (int i = 0; i < attempts && !camelStatus.isStopped(); i++) {
                if (i == 0 || forcedShutdown || camelStatus.isStopping()) {
                    LOG.debug("Camel context still running, waiting 1 second more...");
                } else {
                    try {
                        LOG.info("Forcing Camel context shutdown...");
                        this.camelContext.stop();
                        forcedShutdown = true;
                    } catch (Exception ex) {
                        LOG.warn("Unable to stop Camel context", ex);
                    }
                }

                Thread.sleep(1000);
                camelStatus = this.camelContext.getStatus();
            }

            if (!camelStatus.isStopped()) {
                throw new IllegalStateException("Camel context not stopped after " + attempts + " seconds");
            }
            LOG.info("Camel context stopped");
        }
    }

}

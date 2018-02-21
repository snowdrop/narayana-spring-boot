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

package me.snowdrop.boot.narayana.autoconfigure;

import java.io.File;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import me.snowdrop.boot.narayana.core.NarayanaBeanFactoryPostProcessor;
import me.snowdrop.boot.narayana.core.NarayanaConfigurationBean;
import me.snowdrop.boot.narayana.core.NarayanaRecoveryManagerBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.transaction.jta.JtaProperties;
import org.springframework.boot.jta.XAConnectionFactoryWrapper;
import org.springframework.boot.jta.XADataSourceWrapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.FileSystemUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class NarayanaJtaConfigurationTest {

    private AnnotationConfigApplicationContext context;

    @Before
    public void cleanUpLogs() {
        FileSystemUtils.deleteRecursively(new File("target/transaction-logs"));
    }

    @After
    public void closeContext() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void narayanaSanityCheck() throws Exception {
        this.context = new AnnotationConfigApplicationContext(JtaProperties.class, NarayanaJtaConfiguration.class);
        this.context.getBean(NarayanaConfigurationBean.class);
        this.context.getBean(UserTransaction.class);
        this.context.getBean(TransactionManager.class);
        this.context.getBean(XADataSourceWrapper.class);
        this.context.getBean(XAConnectionFactoryWrapper.class);
        this.context.getBean(NarayanaBeanFactoryPostProcessor.class);
        this.context.getBean(JtaTransactionManager.class);
        this.context.getBean(RecoveryManagerService.class);
    }

    @Test
    public void narayanaRecoveryManagerBeanCanBeCustomized() {
        this.context = new AnnotationConfigApplicationContext();
        this.context.register(CustomNarayanaRecoveryManagerConfiguration.class, JtaProperties.class,
                NarayanaJtaConfiguration.class);
        this.context.refresh();
        assertThat(this.context.getBean(NarayanaRecoveryManagerBean.class))
                .isInstanceOf(CustomNarayanaRecoveryManagerBean.class);
    }

    @Configuration
    public static class CustomNarayanaRecoveryManagerConfiguration {

        @Bean
        public NarayanaRecoveryManagerBean customRecoveryManagerBean(RecoveryManagerService recoveryManagerService) {
            return new CustomNarayanaRecoveryManagerBean(recoveryManagerService);
        }

    }

    static final class CustomNarayanaRecoveryManagerBean extends NarayanaRecoveryManagerBean {

        private CustomNarayanaRecoveryManagerBean(RecoveryManagerService recoveryManagerService) {
            super(recoveryManagerService);
        }

    }

}

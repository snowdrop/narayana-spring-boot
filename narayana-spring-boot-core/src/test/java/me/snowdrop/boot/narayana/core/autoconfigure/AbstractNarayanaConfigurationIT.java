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

package me.snowdrop.boot.narayana.core.autoconfigure;

import java.io.File;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import me.snowdrop.boot.narayana.core.properties.NarayanaPropertiesInitializer;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.autoconfigure.transaction.jta.JtaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class AbstractNarayanaConfigurationIT {

    private AnnotationConfigApplicationContext context;

    @After
    public void closeContext() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void allBeansShouldBeLoaded() {
        this.context = new AnnotationConfigApplicationContext(JtaProperties.class, TestNarayanaConfiguration.class);
        this.context.getBean(NarayanaPropertiesInitializer.class);
        this.context.getBean(UserTransaction.class);
        this.context.getBean(TransactionManager.class);
        this.context.getBean(JtaTransactionManager.class);
        this.context.getBean(RecoveryManagerService.class);
        this.context.getBean(XARecoveryModule.class);
    }

    @Configuration
    @EnableConfigurationProperties({
            JtaProperties.class,
            NarayanaProperties.class
    })
    private static class TestNarayanaConfiguration extends AbstractNarayanaConfiguration {

        TestNarayanaConfiguration(JtaProperties jtaProperties,
                ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
            super(jtaProperties, transactionManagerCustomizers);
        }

        @Override
        protected File getLogDir() {
            return new File("target");
        }
    }

}

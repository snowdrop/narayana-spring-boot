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

package me.snowdrop.boot.narayana;

import java.io.File;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import me.snowdrop.boot.narayana.core.autoconfigure.NarayanaBeanFactoryPostProcessor;
import me.snowdrop.boot.narayana.core.properties.NarayanaPropertiesInitializer;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.transaction.jta.JtaProperties;
import org.springframework.boot.jta.XAConnectionFactoryWrapper;
import org.springframework.boot.jta.XADataSourceWrapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.FileSystemUtils;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SpringBoot1NarayanaConfigurationIT {

    private AnnotationConfigApplicationContext context;

    @After
    public void closeContext() {
        if (this.context != null) {
            this.context.close();
        }
        FileSystemUtils.deleteRecursively(new File("transaction-logs"));
    }

    @Test
    public void allBeansShouldBeLoaded() {
        this.context = new AnnotationConfigApplicationContext(JtaProperties.class, SpringBoot1NarayanaConfiguration.class);
        this.context.getBean(NarayanaBeanFactoryPostProcessor.class);
        this.context.getBean(XADataSourceWrapper.class);
        this.context.getBean(XAConnectionFactoryWrapper.class);
        this.context.getBean(NarayanaPropertiesInitializer.class);
        this.context.getBean(UserTransaction.class);
        this.context.getBean(TransactionManager.class);
        this.context.getBean(JtaTransactionManager.class);
        this.context.getBean(RecoveryManagerService.class);
        this.context.getBean(XARecoveryModule.class);
    }

}

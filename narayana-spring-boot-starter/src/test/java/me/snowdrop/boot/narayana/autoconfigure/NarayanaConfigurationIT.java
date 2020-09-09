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
import java.util.Properties;

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import me.snowdrop.boot.narayana.core.jdbc.GenericXADataSourceWrapper;
import me.snowdrop.boot.narayana.core.jdbc.PooledXADataSourceWrapper;
import me.snowdrop.boot.narayana.core.properties.NarayanaPropertiesInitializer;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.FileSystemUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class NarayanaConfigurationIT {

    private AnnotationConfigApplicationContext context;

    @After
    public void closeContext() {
        if (this.context != null) {
            this.context.close();
        }
        FileSystemUtils.deleteRecursively(new File("transaction-logs"));
    }

    @Test
    public void allDefaultBeansShouldBeLoaded() {
        this.context = new AnnotationConfigApplicationContext(NarayanaConfiguration.class);
        this.context.getBean(NarayanaBeanFactoryPostProcessor.class);
        this.context.getBean(XADataSourceWrapper.class);
        this.context.getBean(XAConnectionFactoryWrapper.class);
        this.context.getBean(NarayanaPropertiesInitializer.class);
        this.context.getBean(UserTransaction.class);
        this.context.getBean(TransactionManager.class);
        this.context.getBean(TransactionSynchronizationRegistry.class);
        this.context.getBean(JtaTransactionManager.class);
        this.context.getBean(RecoveryManagerService.class);
        this.context.getBean(XARecoveryModule.class);
    }

    @Test
    public void genericXaDataSourceWrapperShouldBeLoaded() {
        Properties properties = new Properties();
        properties.put("narayana.dbcp.enabled", "false");
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);

        this.context = new AnnotationConfigApplicationContext();
        this.context.register(NarayanaConfiguration.class);
        this.context.getEnvironment().getPropertySources().addFirst(propertySource);
        this.context.refresh();

        XADataSourceWrapper xaDataSourceWrapper = this.context.getBean(XADataSourceWrapper.class);
        assertThat(xaDataSourceWrapper).isInstanceOf(GenericXADataSourceWrapper.class);
    }

    @Test
    public void pooledXaDataSourceWrapperShouldBeLoaded() {
        Properties properties = new Properties();
        properties.put("narayana.dbcp.enabled", "true");
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);

        this.context = new AnnotationConfigApplicationContext();
        this.context.register(NarayanaConfiguration.class);
        this.context.getEnvironment().getPropertySources().addFirst(propertySource);
        this.context.refresh();

        XADataSourceWrapper xaDataSourceWrapper = this.context.getBean(XADataSourceWrapper.class);
        assertThat(xaDataSourceWrapper).isInstanceOf(PooledXADataSourceWrapper.class);
    }

}

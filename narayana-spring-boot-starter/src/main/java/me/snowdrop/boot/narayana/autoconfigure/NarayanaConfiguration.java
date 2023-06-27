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

package me.snowdrop.boot.narayana.autoconfigure;

import java.io.File;

import jakarta.jms.Message;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import me.snowdrop.boot.narayana.core.jdbc.GenericXADataSourceWrapper;
import me.snowdrop.boot.narayana.core.jms.GenericXAConnectionFactoryWrapper;
import me.snowdrop.boot.narayana.core.jms.PooledXAConnectionFactoryWrapper;
import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import me.snowdrop.boot.narayana.core.properties.NarayanaPropertiesInitializer;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.XADataSourceWrapper;
import org.springframework.boot.jms.XAConnectionFactoryWrapper;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.StringUtils;

/**
 * JTA Configuration for <a href="http://narayana.io/">Narayana</a>.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Configuration
@EnableConfigurationProperties(NarayanaProperties.class)
@ConditionalOnProperty(prefix = "spring.jta", value = "enabled", matchIfMissing = true)
@ConditionalOnClass({
        Transaction.class,
        JtaTransactionManager.class,
        XAResourceRecoveryRegistry.class,
        com.arjuna.ats.jta.UserTransaction.class
})
@ConditionalOnMissingBean(PlatformTransactionManager.class)
@AutoConfigureBefore(JtaAutoConfiguration.class)
public class NarayanaConfiguration {

    private final TransactionManagerCustomizers transactionManagerCustomizers;

    public NarayanaConfiguration(ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        this.transactionManagerCustomizers = transactionManagerCustomizers.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    public static NarayanaBeanFactoryPostProcessor narayanaBeanFactoryPostProcessor() {
        return new NarayanaBeanFactoryPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public NarayanaPropertiesInitializer narayanaPropertiesInitializer(NarayanaProperties properties) {
        initLogDir(properties);
        return new NarayanaPropertiesInitializer(properties);
    }

    @Bean
    @DependsOn("narayanaPropertiesInitializer")
    @ConditionalOnMissingBean
    public UserTransaction narayanaUserTransaction() {
        return com.arjuna.ats.jta.UserTransaction.userTransaction();
    }

    @Bean
    @DependsOn("narayanaPropertiesInitializer")
    @ConditionalOnMissingBean
    public TransactionManager narayanaTransactionManager() {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    @Bean
    @DependsOn("narayanaPropertiesInitializer")
    @ConditionalOnMissingBean
    public TransactionSynchronizationRegistry narayanaTransactionSynchronizationRegistry() {
        return jtaPropertyManager.getJTAEnvironmentBean().getTransactionSynchronizationRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public JtaTransactionManager transactionManager(UserTransaction userTransaction,
            TransactionManager transactionManager,
            TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, transactionManager);
        jtaTransactionManager.setTransactionSynchronizationRegistry(transactionSynchronizationRegistry);
        if (this.transactionManagerCustomizers != null) {
            this.transactionManagerCustomizers.customize(jtaTransactionManager);
        }
        return jtaTransactionManager;
    }

    @Bean(destroyMethod = "stop")
    @DependsOn("narayanaPropertiesInitializer")
    @ConditionalOnMissingBean
    public RecoveryManagerService recoveryManagerService() {
        RecoveryManager.delayRecoveryManagerThread();
        RecoveryManagerService recoveryManagerService = new RecoveryManagerService();
        recoveryManagerService.create();
        recoveryManagerService.start();
        return recoveryManagerService;
    }

    @Bean
    @DependsOn("recoveryManagerService")
    @ConditionalOnMissingBean
    public XARecoveryModule xaRecoveryModule() {
        return XARecoveryModule.getRegisteredXARecoveryModule();
    }

    private void initLogDir(NarayanaProperties properties) {
        if (StringUtils.hasText(properties.getLogDir())) {
            return;
        }

        properties.setLogDir(getLogDir().getAbsolutePath());
    }

    private File getLogDir() {
        File home = new ApplicationHome().getDir();
        return new File(home, "transaction-logs");
    }

    /**
     * Generic data source wrapper configuration.
     */
    static class GenericJdbcConfiguration {

        @Bean
        @ConditionalOnMissingBean(XADataSourceWrapper.class)
        public XADataSourceWrapper xaDataSourceWrapper(NarayanaProperties narayanaProperties,
                XARecoveryModule xaRecoveryModule) {
            return new GenericXADataSourceWrapper(xaRecoveryModule,
                    narayanaProperties.getRecoveryDbCredentials());
        }

    }

    /**
     * JMS connection factory wrapper configuration.
     */
    @ConditionalOnProperty(name = "narayana.messaginghub.enabled", havingValue = "false", matchIfMissing = true)
    @ConditionalOnClass(Message.class)
    static class GenericJmsConfiguration {

        @Bean
        @ConditionalOnMissingBean(XAConnectionFactoryWrapper.class)
        public XAConnectionFactoryWrapper xaConnectionFactoryWrapper(TransactionManager transactionManager,
                XARecoveryModule xaRecoveryModule, NarayanaProperties narayanaProperties) {
            return new GenericXAConnectionFactoryWrapper(transactionManager, xaRecoveryModule,
                    narayanaProperties.getRecoveryJmsCredentials());
        }

    }

    @ConditionalOnProperty(name = "narayana.messaginghub.enabled", havingValue = "true")
    @ConditionalOnClass(Message.class)
    static class PooledJmsConfiguration {

        @Bean
        @ConditionalOnMissingBean(XAConnectionFactoryWrapper.class)
        public XAConnectionFactoryWrapper xaConnectionFactoryWrapper(TransactionManager transactionManager,
                XARecoveryModule xaRecoveryModule, NarayanaProperties narayanaProperties) {
            return new PooledXAConnectionFactoryWrapper(transactionManager, xaRecoveryModule,
                    narayanaProperties.getMessaginghub(),
                    narayanaProperties.getRecoveryJmsCredentials());
        }

    }
}

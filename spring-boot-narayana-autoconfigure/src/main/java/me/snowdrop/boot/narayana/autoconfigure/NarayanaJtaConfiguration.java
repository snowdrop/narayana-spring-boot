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

import javax.jms.Message;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import me.snowdrop.boot.narayana.core.NarayanaBeanFactoryPostProcessor;
import me.snowdrop.boot.narayana.core.NarayanaConfigurationBean;
import me.snowdrop.boot.narayana.core.NarayanaProperties;
import me.snowdrop.boot.narayana.core.NarayanaRecoveryManagerBean;
import me.snowdrop.boot.narayana.core.NarayanaXAConnectionFactoryWrapper;
import me.snowdrop.boot.narayana.core.NarayanaXADataSourceWrapper;
import org.jboss.narayana.jta.jms.TransactionHelper;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationHome;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.autoconfigure.transaction.jta.JtaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jta.XAConnectionFactoryWrapper;
import org.springframework.boot.jta.XADataSourceWrapper;
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
@ConditionalOnClass(
        { JtaTransactionManager.class, com.arjuna.ats.jta.UserTransaction.class, XAResourceRecoveryRegistry.class })
@ConditionalOnMissingBean(PlatformTransactionManager.class)
@EnableConfigurationProperties({ JtaProperties.class, NarayanaProperties.class })
@AutoConfigureBefore(name = "org.springframework.boot.autoconfigure.transaction.jta.NarayanaJtaConfiguration")
public class NarayanaJtaConfiguration {

    private final JtaProperties jtaProperties;

    private final TransactionManagerCustomizers transactionManagerCustomizers;

    public NarayanaJtaConfiguration(JtaProperties jtaProperties,
            ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        this.jtaProperties = jtaProperties;
        this.transactionManagerCustomizers = transactionManagerCustomizers.getIfAvailable();
    }

    @Bean
    @ConditionalOnMissingBean
    public NarayanaConfigurationBean narayanaConfiguration(NarayanaProperties properties) {
        properties.setLogDir(getLogDir().getAbsolutePath());
        if (this.jtaProperties.getTransactionManagerId() != null) {
            properties.setTransactionManagerId(this.jtaProperties.getTransactionManagerId());
        }
        return new NarayanaConfigurationBean(properties);
    }

    private File getLogDir() {
        if (StringUtils.hasLength(this.jtaProperties.getLogDir())) {
            return new File(this.jtaProperties.getLogDir());
        }
        File home = new ApplicationHome().getDir();
        return new File(home, "transaction-logs");
    }

    @Bean
    @DependsOn("narayanaConfiguration")
    @ConditionalOnMissingBean
    public UserTransaction narayanaUserTransaction() {
        return com.arjuna.ats.jta.UserTransaction.userTransaction();
    }

    @Bean
    @DependsOn("narayanaConfiguration")
    @ConditionalOnMissingBean
    public TransactionManager narayanaTransactionManager() {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    @Bean
    @DependsOn("narayanaConfiguration")
    public RecoveryManagerService narayanaRecoveryManagerService() {
        RecoveryManager.delayRecoveryManagerThread();
        return new RecoveryManagerService();
    }

    @Bean
    @ConditionalOnMissingBean
    public NarayanaRecoveryManagerBean narayanaRecoveryManager(RecoveryManagerService recoveryManagerService) {
        return new NarayanaRecoveryManagerBean(recoveryManagerService);
    }

    @Bean
    public JtaTransactionManager transactionManager(UserTransaction userTransaction,
            TransactionManager transactionManager) {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager(userTransaction, transactionManager);
        if (this.transactionManagerCustomizers != null) {
            this.transactionManagerCustomizers.customize(jtaTransactionManager);
        }
        return jtaTransactionManager;
    }

    @Bean
    @ConditionalOnMissingBean(XADataSourceWrapper.class)
    public XADataSourceWrapper xaDataSourceWrapper(NarayanaRecoveryManagerBean narayanaRecoveryManagerBean,
            NarayanaProperties narayanaProperties) {
        return new NarayanaXADataSourceWrapper(narayanaRecoveryManagerBean, narayanaProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public static NarayanaBeanFactoryPostProcessor narayanaBeanFactoryPostProcessor() {
        return new NarayanaBeanFactoryPostProcessor();
    }

    /**
     * JMS specific JTA configuration.
     */
    @Configuration
    @ConditionalOnClass({ Message.class, TransactionHelper.class })
    static class NarayanaJtaJmsConfiguration {

        @Bean
        @ConditionalOnMissingBean(XAConnectionFactoryWrapper.class)
        public NarayanaXAConnectionFactoryWrapper xaConnectionFactoryWrapper(TransactionManager transactionManager,
                NarayanaRecoveryManagerBean narayanaRecoveryManagerBean, NarayanaProperties narayanaProperties) {
            return new NarayanaXAConnectionFactoryWrapper(transactionManager, narayanaRecoveryManagerBean,
                    narayanaProperties);
        }

    }

}

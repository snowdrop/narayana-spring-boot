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

import javax.jms.Message;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import me.snowdrop.boot.narayana.core.autoconfigure.AbstractNarayanaConfiguration;
import me.snowdrop.boot.narayana.core.autoconfigure.NarayanaBeanFactoryPostProcessor;
import me.snowdrop.boot.narayana.core.jdbc.NarayanaXADataSourceWrapper;
import me.snowdrop.boot.narayana.core.jms.NarayanaXAConnectionFactoryWrapper;
import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import org.jboss.narayana.jta.jms.TransactionHelper;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationHome;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.jta.JtaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jta.XAConnectionFactoryWrapper;
import org.springframework.boot.jta.XADataSourceWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.StringUtils;

/**
 * JTA Configuration for <a href="http://narayana.io/">Narayana</a>.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Configuration
@EnableConfigurationProperties({
        JtaProperties.class,
        NarayanaProperties.class
})
@ConditionalOnProperty(prefix = "spring.jta", value = "enabled", matchIfMissing = true)
@ConditionalOnClass({
        Transaction.class,
        JtaTransactionManager.class,
        XAResourceRecoveryRegistry.class,
        com.arjuna.ats.jta.UserTransaction.class
})
@ConditionalOnMissingBean(PlatformTransactionManager.class)
@AutoConfigureBefore(JtaAutoConfiguration.class)
public class SpringBoot1NarayanaConfiguration extends AbstractNarayanaConfiguration {

    private final JtaProperties jtaProperties;

    public SpringBoot1NarayanaConfiguration(JtaProperties jtaProperties,
            ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        super(jtaProperties, transactionManagerCustomizers);
        this.jtaProperties = jtaProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public static NarayanaBeanFactoryPostProcessor narayanaBeanFactoryPostProcessor() {
        return new NarayanaBeanFactoryPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(XADataSourceWrapper.class)
    public XADataSourceWrapper xaDataSourceWrapper(NarayanaProperties narayanaProperties,
            XARecoveryModule xaRecoveryModule) {
        NarayanaXADataSourceWrapper narayanaXADataSourceWrapper =
                new NarayanaXADataSourceWrapper(narayanaProperties, xaRecoveryModule);
        return new DelegatingXADataSourceWrapper(narayanaXADataSourceWrapper);
    }

    @Override
    protected File getLogDir() {
        if (StringUtils.hasLength(this.jtaProperties.getLogDir())) {
            return new File(this.jtaProperties.getLogDir());
        }
        File home = new ApplicationHome().getDir();
        return new File(home, "transaction-logs");
    }

    /**
     * JMS specific JTA configuration.
     */
    @Configuration
    @ConditionalOnClass({
            Message.class,
            TransactionHelper.class
    })
    static class NarayanaJmsConfiguration {

        @Bean
        @ConditionalOnMissingBean(XAConnectionFactoryWrapper.class)
        public XAConnectionFactoryWrapper xaConnectionFactoryWrapper(TransactionManager transactionManager,
                XARecoveryModule xaRecoveryModule, NarayanaProperties narayanaProperties) {
            NarayanaXAConnectionFactoryWrapper narayanaXAConnectionFactoryWrapper =
                    new NarayanaXAConnectionFactoryWrapper(transactionManager, xaRecoveryModule, narayanaProperties);
            return new DelegatingXAConnectionFactoryWrapper(narayanaXAConnectionFactoryWrapper);
        }

    }

}

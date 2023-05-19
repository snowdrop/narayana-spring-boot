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

import javax.sql.DataSource;

import jakarta.jms.ConnectionFactory;
import jakarta.transaction.TransactionManager;

import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link NarayanaBeanFactoryPostProcessor}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class NarayanaBeanFactoryPostProcessorTests {

    private AnnotationConfigApplicationContext context;

    @Test
    void setsDependsOn() {
        DefaultListableBeanFactory beanFactory = Mockito.spy(new DefaultListableBeanFactory());
        this.context = new AnnotationConfigApplicationContext(beanFactory);
        this.context.register(Config.class);
        this.context.refresh();
        verify(beanFactory).registerDependentBean("transactionManager", "dataSource");
        verify(beanFactory).registerDependentBean("transactionManager", "connectionFactory");
        verify(beanFactory).registerDependentBean("recoveryManagerService", "dataSource");
        verify(beanFactory).registerDependentBean("recoveryManagerService", "connectionFactory");
        this.context.close();
    }

    @Configuration
    static class Config {

        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class);
        }

        @Bean
        public ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }

        @Bean
        public TransactionManager transactionManager() {
            return mock(TransactionManager.class);
        }

        @Bean
        public RecoveryManagerService recoveryManagerService() {
            return mock(RecoveryManagerService.class);
        }

        @Bean
        public static NarayanaBeanFactoryPostProcessor narayanaBeanFactoryPostProcessor() {
            return new NarayanaBeanFactoryPostProcessor();
        }
    }
}

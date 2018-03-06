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

import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.autoconfigure.transaction.jta.JtaProperties;
import org.springframework.transaction.jta.JtaTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractNarayanaConfigurationTest {

    @Mock
    private File mockLogDir;

    @Mock
    private JtaProperties mockJtaProperties;

    @Mock
    private ObjectProvider<TransactionManagerCustomizers> mockTransactionManagerCustomizersProvider;

    @Mock
    private TransactionManagerCustomizers mockTransactionManagerCustomizers;

    @Mock
    private NarayanaProperties mockNarayanaProperties;

    @Mock
    private UserTransaction mockUserTransaction;

    @Mock
    private TransactionManager mockTransactionManager;

    private TestNarayanaConfiguration configuration;

    @Before
    public void before() {
        this.configuration = new TestNarayanaConfiguration(this.mockLogDir, this.mockJtaProperties,
                this.mockTransactionManagerCustomizersProvider);
    }

    @Test
    public void narayanaPropertiesInitializerShouldSetLogDir() {
        given(this.mockLogDir.getAbsolutePath()).willReturn("path");
        this.configuration.narayanaPropertiesInitializer(this.mockNarayanaProperties);
        verify(this.mockNarayanaProperties).setLogDir("path");
    }

    @Test
    public void narayanaPropertiesInitializerShouldNotSetLogDirIfPropertyExists() {
        given(this.mockNarayanaProperties.getLogDir()).willReturn("properties-path");
        this.configuration.narayanaPropertiesInitializer(this.mockNarayanaProperties);
        verify(this.mockNarayanaProperties, times(0)).setLogDir(anyString());
    }

    @Test
    public void narayanaPropertiesInitializerShouldSetTransactionManagerId() {
        given(this.mockJtaProperties.getTransactionManagerId()).willReturn("id");
        this.configuration.narayanaPropertiesInitializer(this.mockNarayanaProperties);
        verify(this.mockNarayanaProperties).setTransactionManagerId("id");
    }

    @Test
    public void narayanaPropertiesInitializerShouldNotSetTransactionManagerIdIfPropertyExists() {
        // given null transaction manager ID
        this.configuration.narayanaPropertiesInitializer(this.mockNarayanaProperties);
        verify(this.mockNarayanaProperties, times(0)).setTransactionManagerId(anyString());
    }

    @Test
    public void jtaTransactionManagerShouldBeCreated() {
        JtaTransactionManager jtaTransactionManager =
                this.configuration.transactionManager(this.mockUserTransaction, this.mockTransactionManager);
        assertThat(jtaTransactionManager.getUserTransaction()).isEqualTo(this.mockUserTransaction);
        assertThat(jtaTransactionManager.getTransactionManager()).isEqualTo(this.mockTransactionManager);
    }

    @Test
    public void jtaTransactionManagerShouldBeCustomized() {
        given(this.mockTransactionManagerCustomizersProvider.getIfAvailable()).willReturn(
                this.mockTransactionManagerCustomizers);
        this.configuration = new TestNarayanaConfiguration(this.mockLogDir, this.mockJtaProperties,
                this.mockTransactionManagerCustomizersProvider);
        JtaTransactionManager jtaTransactionManager =
                this.configuration.transactionManager(this.mockUserTransaction, this.mockTransactionManager);
        verify(this.mockTransactionManagerCustomizers).customize(jtaTransactionManager);
    }

    private static class TestNarayanaConfiguration extends AbstractNarayanaConfiguration {

        private final File logDir;

        TestNarayanaConfiguration(File logDir, JtaProperties jtaProperties,
                ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
            super(jtaProperties, transactionManagerCustomizers);
            this.logDir = logDir;
        }

        @Override
        protected File getLogDir() {
            return this.logDir;
        }
    }

}

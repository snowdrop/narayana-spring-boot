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

import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;

import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.transaction.jta.JtaTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@ExtendWith(MockitoExtension.class)
class NarayanaConfigurationTest {

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

    @Mock
    private TransactionSynchronizationRegistry mockTransactionSynchronizationRegistry;

    private NarayanaConfiguration configuration;

    @BeforeEach
    void before() {
        this.configuration = new NarayanaConfiguration(this.mockTransactionManagerCustomizersProvider);
    }

    @Test
    void narayanaPropertiesInitializerShouldUseNarayanaLogDir() {
        given(this.mockNarayanaProperties.getLogDir()).willReturn("narayana-log-dir");
        this.configuration.narayanaPropertiesInitializer(this.mockNarayanaProperties);
        verify(this.mockNarayanaProperties, times(0)).setLogDir(anyString());
    }

    @Test
    void narayanaPropertiesInitializerShouldUseDefaultLogDir() {
        this.configuration.narayanaPropertiesInitializer(this.mockNarayanaProperties);
        File applicationHomeDir = new ApplicationHome().getDir();
        File expectedLogDir = new File(applicationHomeDir, "transaction-logs");
        verify(this.mockNarayanaProperties).setLogDir(expectedLogDir.getAbsolutePath());
    }

    @Test
    void jtaTransactionManagerShouldBeCreated() {
        JtaTransactionManager jtaTransactionManager = this.configuration.transactionManager(
                this.mockUserTransaction, this.mockTransactionManager, this.mockTransactionSynchronizationRegistry);
        assertThat(jtaTransactionManager.getUserTransaction()).isEqualTo(this.mockUserTransaction);
        assertThat(jtaTransactionManager.getTransactionManager()).isEqualTo(this.mockTransactionManager);
        assertThat(jtaTransactionManager.getTransactionSynchronizationRegistry())
                .isEqualTo(this.mockTransactionSynchronizationRegistry);
    }

    @Test
    void jtaTransactionManagerShouldBeCustomized() {
        given(this.mockTransactionManagerCustomizersProvider.getIfAvailable()).willReturn(
                this.mockTransactionManagerCustomizers);
        this.configuration = new NarayanaConfiguration(this.mockTransactionManagerCustomizersProvider);
        JtaTransactionManager jtaTransactionManager = this.configuration.transactionManager(
                this.mockUserTransaction, this.mockTransactionManager, this.mockTransactionSynchronizationRegistry);
        verify(this.mockTransactionManagerCustomizers).customize(jtaTransactionManager);
    }
}

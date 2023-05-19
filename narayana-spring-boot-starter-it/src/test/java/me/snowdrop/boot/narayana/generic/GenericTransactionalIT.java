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

package me.snowdrop.boot.narayana.generic;

import java.time.Duration;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import io.agroal.springframework.boot.AgroalDataSourceConfiguration;
import me.snowdrop.boot.narayana.app.EntriesService;
import me.snowdrop.boot.narayana.app.Entry;
import me.snowdrop.boot.narayana.app.MessagesService;
import me.snowdrop.boot.narayana.app.TestApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@SpringBootTest(classes = TestApplication.class)
@EnableAutoConfiguration(exclude = AgroalDataSourceConfiguration.class)
public class GenericTransactionalIT {

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private EntriesService entriesService;

    @BeforeEach
    void before() {
        this.messagesService.clearReceivedMessages();
        this.entriesService.clearEntries();
    }

    @AfterEach
    void after() {
        try {
            this.transactionManager.rollback();
        } catch (Throwable ignored) {
        }
    }

    @Test
    void shouldCommitTransaction() throws Exception {
        this.transactionManager.begin();
        this.messagesService.sendMessage("test-message");
        Entry entry = this.entriesService.createEntry("test-value");

        Transaction transaction = this.transactionManager.suspend();
        assertThat(this.messagesService.getReceivedMessages())
                .as("Received messages should be empty until transaction is committed")
                .isEmpty();
        assertThat(this.entriesService.getEntries())
                .as("Entries should be empty until transaction is committed")
                .isEmpty();

        this.transactionManager.resume(transaction);
        this.transactionManager.commit();
        await("Wait until the test message is received")
                .atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(this.messagesService.getReceivedMessages())
                        .as("Test message should have been received after transaction was committed")
                        .containsOnly("test-message")
                );
        assertThat(this.entriesService.getEntries())
                .as("Test entry should exist after transaction was committed")
                .containsOnly(entry);
    }

    @Test
    void shouldRollbackTransaction() throws Exception {
        this.transactionManager.begin();
        this.messagesService.sendMessage("test-message");
        this.entriesService.createEntry("test-value");

        Transaction transaction = this.transactionManager.suspend();
        assertThat(this.messagesService.getReceivedMessages())
                .as("Received messages should be empty until transaction is committed")
                .isEmpty();
        assertThat(this.entriesService.getEntries())
                .as("Entries should be empty until transaction is committed")
                .isEmpty();

        this.transactionManager.resume(transaction);
        this.transactionManager.rollback();
        assertThat(this.messagesService.getReceivedMessages())
                .as("Received messages should be empty after transaction rollback")
                .isEmpty();
        assertThat(this.entriesService.getEntries())
                .as("Entries should be empty after transaction rollback")
                .isEmpty();
    }
}

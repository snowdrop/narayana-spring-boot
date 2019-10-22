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

package me.snowdrop.boot.narayana.generic;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import me.snowdrop.boot.narayana.app.EntriesService;
import me.snowdrop.boot.narayana.app.Entry;
import me.snowdrop.boot.narayana.app.MessagesService;
import me.snowdrop.boot.narayana.app.TestApplication;
import me.snowdrop.boot.narayana.utils.BytemanHelper;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(BMUnitRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class GenericRecoveryIT {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Mock
    private XAResource xaResource;

    @Mock
    private XAResourceRecoveryHelper xaResourceRecoveryHelper;

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private EntriesService entriesService;

    @Autowired
    private TransactionManager transactionManager;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        this.messagesService.clearReceivedMessages();
        this.entriesService.clearEntries();
        BytemanHelper.reset();
    }

    @Test
    @BMRule(name = "Fail before commit",
            targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
            targetMethod = "phase2Commit",
            targetLocation = "ENTRY",
            helper = "me.snowdrop.boot.narayana.utils.BytemanHelper",
            action = "incrementCommitsCounter(); failFirstCommit($0.get_uid());")
    public void testCrashBeforeCommit() throws Exception {
        // Setup dummy XAResource and its recovery helper
        setupXaMocks();

        this.transactionManager.begin();
        this.transactionManager.getTransaction()
                .enlistResource(this.xaResource);
        this.messagesService.sendMessage("test-message");
        Entry entry = this.entriesService.createEntry("test-value");
        try {
            // Byteman rule will cause commit to fail
            this.transactionManager.commit();
            fail("Exception was expected");
        } catch (Exception ignored) {
        }

        // Just after crash message and entry shouldn't be available
        assertThat(this.messagesService.getReceivedMessages())
                .isEmpty();
        assertThat(this.entriesService.getEntries())
                .isEmpty();

        await("Wait for the recovery to happen")
                .atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    assertThat(this.messagesService.getReceivedMessages())
                            .as("Test message should have been received after transaction was committed")
                            .containsOnly("test-message");
                    assertThat(this.entriesService.getEntries())
                            .as("Test entry should exist after transaction was committed")
                            .containsOnly(entry);
                });
    }

    private void setupXaMocks() throws Exception {
        List<Xid> xids = new LinkedList<>();
        // Save Xid provided during prepare
        when(this.xaResource.prepare(any(Xid.class)))
                .then(i -> {
                    xids.add((Xid) i.getArguments()[0]);
                    return XAResource.XA_OK;
                });
        // Return Xids when recovering
        when(this.xaResource.recover(anyInt()))
                .then(i -> xids.toArray(new Xid[xids.size()]));
        // Return XAResource when recovering
        when(this.xaResourceRecoveryHelper.getXAResources())
                .thenReturn(new XAResource[]{ this.xaResource });
    }
}

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

package dev.snowdrop.boot.narayana.core.properties;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.arjuna.ats.arjuna.common.CoordinatorEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link NarayanaPropertiesInitializer}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
class NarayanaPropertiesInitializerTests {

    @AfterEach
    void after() throws NoSuchFieldException, IllegalAccessException {
        // BeanPopulator holds instances in a static private map, so in order to reset it we need reflection
        Field beanInstancesField = BeanPopulator.class.getDeclaredField("beanInstances");
        beanInstancesField.setAccessible(true);
        ((Map<?, ?>) beanInstancesField.get(null)).clear();
    }

    @Test
    void shouldSetDefaultProperties() {
        NarayanaProperties narayanaProperties = new NarayanaProperties();
        NarayanaPropertiesInitializer narayanaPropertiesInitializer =
                new NarayanaPropertiesInitializer(narayanaProperties);
        narayanaPropertiesInitializer.afterPropertiesSet();

        assertThat(BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class)
                .getNodeIdentifier()).isEqualTo("1");
        assertThat(BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
                .getXaRecoveryNodes()).containsOnly("1");
        assertThat(BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                .getObjectStoreDir()).contains("ObjectStore");
        assertThat(BeanPopulator
                .getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore")
                .getObjectStoreDir()).endsWith("ObjectStore");
        assertThat(BeanPopulator
                .getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore")
                .getObjectStoreDir()).endsWith("ObjectStore");
        assertThat(BeanPopulator.getDefaultInstance(CoordinatorEnvironmentBean.class)
                .isCommitOnePhase()).isTrue();
        assertThat(BeanPopulator.getDefaultInstance(CoordinatorEnvironmentBean.class)
                .getDefaultTimeout()).isEqualTo(60);
        assertThat(BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class)
                .getPeriodicRecoveryPeriod()).isEqualTo(120);
        assertThat(BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class)
                .getRecoveryBackoffPeriod()).isEqualTo(10);
        assertThat(BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class)
                .getExpiryScanInterval()).isEqualTo(12);

        List<String> xaResourceOrphanFilters = List.of(
                "com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter",
                "com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter",
                "com.arjuna.ats.internal.jta.recovery.arjunacore.JTAActionStatusServiceXAResourceOrphanFilter");
        assertThat(BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
                .getXaResourceOrphanFilterClassNames())
                .isEqualTo(xaResourceOrphanFilters);

        assertThat(BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
                .getXaResourceRecordWrappingPluginClassName())
                .isEqualTo("com.arjuna.ats.internal.jbossatx.jta.XAResourceRecordWrappingPluginImpl");

        assertThat(BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
                .getLastResourceOptimisationInterfaceClassName())
                .isEqualTo("org.jboss.tm.LastResource");

        assertThat(BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
                .getCommitMarkableResourceJNDINames())
                .isEmpty();

        List<String> recoveryModules = List.of(
                "com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule",
                "com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule",
                "com.arjuna.ats.internal.txoj.recovery.TORecoveryModule",
                "com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule");
        assertThat(BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class)
                .getRecoveryModuleClassNames()).isEqualTo(recoveryModules);

        List<String> expiryScanners = List.of(
                "com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner");
        assertThat(BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class)
                .getExpiryScannerClassNames()).isEqualTo(expiryScanners);

        assertThat(BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
                .getXaResourceRecoveryClassNames()).isEmpty();
    }

    @Test
    void shouldSetModifiedProperties() {
        NarayanaProperties narayanaProperties = new NarayanaProperties();
        narayanaProperties.setNodeIdentifier("test-id-1");
        narayanaProperties.setXaRecoveryNodes(List.of("test-id-1", "test-id-2"));
        narayanaProperties.setLogDir("test-dir");
        narayanaProperties.setDefaultTimeout(1);
        narayanaProperties.setPeriodicRecoveryPeriod(2);
        narayanaProperties.setRecoveryBackoffPeriod(3);
        narayanaProperties.setOnePhaseCommit(false);
        narayanaProperties.setXaResourceOrphanFilters(List.of("test-filter-1", "test-filter-2"));
        narayanaProperties.setRecoveryModules(List.of("test-module-1", "test-module-2"));
        narayanaProperties.setExpiryScanners(List.of("test-scanner-1", "test-scanner-2"));

        NarayanaPropertiesInitializer narayanaPropertiesInitializer =
                new NarayanaPropertiesInitializer(narayanaProperties);
        narayanaPropertiesInitializer.afterPropertiesSet();

        assertThat(BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class)
                .getNodeIdentifier()).isEqualTo("test-id-1");
        assertThat(BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
                .getXaRecoveryNodes()).containsExactly("test-id-1", "test-id-2");
        assertThat(BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class)
                .getObjectStoreDir()).isEqualTo("test-dir");
        assertThat(BeanPopulator
                .getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore")
                .getObjectStoreDir()).isEqualTo("test-dir");
        assertThat(BeanPopulator
                .getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore")
                .getObjectStoreDir()).isEqualTo("test-dir");
        assertThat(BeanPopulator.getDefaultInstance(CoordinatorEnvironmentBean.class)
                .isCommitOnePhase()).isFalse();
        assertThat(BeanPopulator.getDefaultInstance(CoordinatorEnvironmentBean.class)
                .getDefaultTimeout()).isEqualTo(1);
        assertThat(BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class)
                .getPeriodicRecoveryPeriod()).isEqualTo(2);
        assertThat(BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class)
                .getRecoveryBackoffPeriod()).isEqualTo(3);
        assertThat(BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class)
                .getXaResourceOrphanFilterClassNames())
                .isEqualTo(List.of("test-filter-1", "test-filter-2"));
        assertThat(BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class)
                .getRecoveryModuleClassNames())
                .isEqualTo(List.of("test-module-1", "test-module-2"));
        assertThat(BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class)
                .getExpiryScannerClassNames())
                .isEqualTo(List.of("test-scanner-1", "test-scanner-2"));
    }

    @Test
    void shouldSetShortenNodeIdentifier() {
        NarayanaProperties narayanaProperties = new NarayanaProperties();
        narayanaProperties.setNodeIdentifier("x".repeat(30));

        narayanaProperties.setShortenNodeIdentifierIfNecessary(true);
        NarayanaPropertiesInitializer narayanaPropertiesInitializer =
                new NarayanaPropertiesInitializer(narayanaProperties);
        narayanaPropertiesInitializer.afterPropertiesSet();

        assertThat(BeanPopulator.getDefaultInstance(CoreEnvironmentBean.class)
                .getNodeIdentifierBytes().length).isEqualTo(28);
    }
}

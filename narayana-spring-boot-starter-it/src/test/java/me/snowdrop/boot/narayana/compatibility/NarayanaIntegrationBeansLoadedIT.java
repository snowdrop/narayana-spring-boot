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

package me.snowdrop.boot.narayana.compatibility;

import java.util.Arrays;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import me.snowdrop.boot.narayana.app.TestApplication;
import me.snowdrop.boot.narayana.autoconfigure.NarayanaBeanFactoryPostProcessor;
import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import me.snowdrop.boot.narayana.core.properties.NarayanaPropertiesInitializer;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.jta.JtaTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = TestApplication.class)
public class NarayanaIntegrationBeansLoadedIT {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Parameter
    public Class<?> beanClass;

    @Autowired
    private ApplicationContext context;

    @Parameters(name = "{index}: {0}")
    public static Iterable<Class<?>> expectedBeans() {
        return Arrays.asList(
                NarayanaBeanFactoryPostProcessor.class,
                NarayanaProperties.class,
                NarayanaPropertiesInitializer.class,
                UserTransactionImple.class,
                TransactionManagerImple.class,
                JtaTransactionManager.class,
                RecoveryManagerService.class,
                XARecoveryModule.class
        );
    }

    @Test
    public void verifyBeanExists() {
        assertThat(this.context.getBean(this.beanClass))
                .as("Verify that 's' bean exists", this.beanClass)
                .isNotNull();
    }

}

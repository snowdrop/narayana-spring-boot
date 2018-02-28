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

package me.snowdrop.boot.narayana.core.recovery;

import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Tests for {@link RecoveryServiceManager}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class RecoveryServiceManagerTests {

    @Mock
    private RecoveryManagerService mockRecoveryManagerService;

    private RecoveryServiceManager recoveryServiceManager;

    @Before
    public void before() {
        this.recoveryServiceManager = new RecoveryServiceManager(this.mockRecoveryManagerService);
    }

    @Test
    public void shouldCreateAndStartRecoveryManagerService() {
        this.recoveryServiceManager.initialize();
        verify(this.mockRecoveryManagerService).create();
        verify(this.mockRecoveryManagerService).start();
    }

    @Test
    public void shouldStopAndDestroyRecoveryManagerService() throws Exception {
        this.recoveryServiceManager.destroy();
        verify(this.mockRecoveryManagerService).stop();
        verify(this.mockRecoveryManagerService).destroy();
    }

}

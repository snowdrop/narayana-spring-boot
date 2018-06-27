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

package me.snowdrop.boot.narayana.core.jdbc;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import me.snowdrop.boot.narayana.core.properties.NarayanaProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link GenericXADataSourceWrapper}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericXADataSourceWrapperTests {

    @Mock
    private XADataSource mockXaDataSource;

    @Mock
    private XARecoveryModule mockXaRecoveryModule;

    @Mock
    private NarayanaProperties mockNarayanaProperties;

    private GenericXADataSourceWrapper wrapper;

    @Before
    public void before() {
        this.wrapper = new GenericXADataSourceWrapper(this.mockNarayanaProperties, this.mockXaRecoveryModule);
    }

    @Test
    public void wrap() throws Exception {
        DataSource wrapped = this.wrapper.wrapDataSource(this.mockXaDataSource);
        assertThat(wrapped).isInstanceOf(NarayanaDataSource.class);
        verify(this.mockXaRecoveryModule).addXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
        verify(this.mockNarayanaProperties).getRecoveryDbUser();
        verify(this.mockNarayanaProperties).getRecoveryDbPass();
    }

    @Test
    public void wrapWithCredentials() throws Exception {
        given(this.mockNarayanaProperties.getRecoveryDbUser()).willReturn("userName");
        given(this.mockNarayanaProperties.getRecoveryDbPass()).willReturn("password");
        DataSource wrapped = this.wrapper.wrapDataSource(this.mockXaDataSource);
        assertThat(wrapped).isInstanceOf(NarayanaDataSource.class);
        verify(this.mockXaRecoveryModule).addXAResourceRecoveryHelper(any(DataSourceXAResourceRecoveryHelper.class));
        verify(this.mockNarayanaProperties, times(2)).getRecoveryDbUser();
        verify(this.mockNarayanaProperties).getRecoveryDbPass();
    }

}

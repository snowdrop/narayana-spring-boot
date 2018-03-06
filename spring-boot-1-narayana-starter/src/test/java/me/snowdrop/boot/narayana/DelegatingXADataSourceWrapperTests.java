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

import javax.sql.XADataSource;

import me.snowdrop.boot.narayana.core.jdbc.NarayanaXADataSourceWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class DelegatingXADataSourceWrapperTests {

    @Mock
    private NarayanaXADataSourceWrapper mockDelegate;

    @Mock
    private XADataSource mockXaDataSource;

    @Test
    public void shouldDelegateToNarayanaWrapper() {
        DelegatingXADataSourceWrapper wrapper = new DelegatingXADataSourceWrapper(this.mockDelegate);
        wrapper.wrapDataSource(this.mockXaDataSource);
        verify(this.mockDelegate).wrapDataSource(this.mockXaDataSource);
    }

}

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

import javax.sql.DataSource;
import javax.sql.XADataSource;

import me.snowdrop.boot.narayana.core.jdbc.NarayanaXADataSourceWrapper;
import org.springframework.boot.jta.XADataSourceWrapper;

/**
 * {@link XADataSourceWrapper} implementation delegating to {@link NarayanaXADataSourceWrapper}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DelegatingXADataSourceWrapper implements XADataSourceWrapper {

    private final NarayanaXADataSourceWrapper delegate;

    public DelegatingXADataSourceWrapper(NarayanaXADataSourceWrapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public DataSource wrapDataSource(XADataSource dataSource) {
        return this.delegate.wrapDataSource(dataSource);
    }

}

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

package me.snowdrop.boot.narayana.core.tx;

import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

import me.snowdrop.boot.narayana.core.jdbc.DataSourceXAResourceRecoveryHelper;
import org.jboss.tm.XAResourceWrapper;

public class DataSourceXAResourceWrapperRecoveryHelper extends DataSourceXAResourceRecoveryHelper implements XAResourceWrapper {

    private static final String PRODUCT_NAME = DataSourceXAResourceWrapperRecoveryHelper.class.getPackage().getImplementationTitle();
    private static final String PRODUCT_VERSION = DataSourceXAResourceWrapperRecoveryHelper.class.getPackage().getImplementationVersion();

    private final String name;

    public DataSourceXAResourceWrapperRecoveryHelper(XADataSource xaDataSource, String name) {
        this(xaDataSource, name, null, null);
    }

    public DataSourceXAResourceWrapperRecoveryHelper(XADataSource xaDataSource, String name, String user, String password) {
        super(xaDataSource, user, password);
        this.name = name != null ? name : "dataSource";
    }

    @Override
    public XAResource getResource() {
        return this;
    }

    @Override
    public String getProductName() {
        return PRODUCT_NAME;
    }

    @Override
    public String getProductVersion() {
        return PRODUCT_VERSION;
    }

    @Override
    public String getJndiName() {
        return this.name;
    }
}

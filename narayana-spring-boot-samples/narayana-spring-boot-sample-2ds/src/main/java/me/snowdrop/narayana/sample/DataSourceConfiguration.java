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

package me.snowdrop.narayana.sample;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import me.snowdrop.boot.narayana.core.jdbc.GenericXADataSourceWrapper;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfiguration {

    @Bean(name = "ds1")
    public DataSource firstDataSource(XARecoveryModule xaRecoveryModule) throws Exception {
        JdbcDataSource h2XaDataSource = new JdbcDataSource();
        h2XaDataSource.setURL("jdbc:h2:mem:ds1;DB_CLOSE_DELAY=-1");
        createDummyTable(h2XaDataSource);

        GenericXADataSourceWrapper wrapper = new GenericXADataSourceWrapper(xaRecoveryModule);
        return wrapper.wrapDataSource(h2XaDataSource);
    }

    @Bean(name = "ds2")
    public DataSource secondDataSource(XARecoveryModule xaRecoveryModule) throws Exception {
        JdbcDataSource h2XaDataSource = new JdbcDataSource();
        h2XaDataSource.setURL("jdbc:h2:mem:ds2;DB_CLOSE_DELAY=-1");
        createDummyTable(h2XaDataSource);

        GenericXADataSourceWrapper wrapper = new GenericXADataSourceWrapper(xaRecoveryModule);
        return wrapper.wrapDataSource(h2XaDataSource);
    }

    private void createDummyTable(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("create table dummy (val int)");
        }
    }
}

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

package dev.snowdrop.boot.narayana.testcontainers;

import java.util.List;

import dev.snowdrop.boot.narayana.app.Entry;
import dev.snowdrop.boot.narayana.generic.GenericRecoveryIT;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("testcontainers")
@Testcontainers
public class MySQLGenericRecoveryIT extends GenericRecoveryIT {

    @Container
    @ServiceConnection
    static JdbcDatabaseContainer<?> mysql = new MySQLContainer<>("mysql:latest")
            .withUsername("root")
            .withPassword("root");

    @Override
    protected void assertEntriesAfterCrash(List<Entry> entries) {
        // Empty because server locks table until successfully recovered.
    }
}

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

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.db2.Db2Container;
import org.testcontainers.junit.jupiter.Container;

public interface DB2ContainerConfiguration {

    @Container
    @ServiceConnection
    JdbcDatabaseContainer<?> db2 = new Db2Container("icr.io/db2_community/db2")
            .acceptLicense()
            .withEnv("PERSISTENT_HOME", "false");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.xa.properties.driverType", () -> 4);
        registry.add("spring.datasource.xa.properties.serverName", db2::getHost);
        registry.add("spring.datasource.xa.properties.portNumber", () -> db2.getMappedPort(Db2Container.DB2_PORT));
        registry.add("spring.datasource.xa.properties.databaseName", db2::getDatabaseName);
    }
}

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

package dev.snowdrop.boot.narayana.testcontainers.pooled;

import dev.snowdrop.boot.narayana.app.TestApplication;
import dev.snowdrop.boot.narayana.pooled.PooledTransactionalIT;
import dev.snowdrop.boot.narayana.testcontainers.MSSQLContainerConfiguration;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("testcontainers")
@Tag("agroal")
@Testcontainers
@SpringBootTest(classes = TestApplication.class, properties = {
        "narayana.messaginghub.enabled=true",
        "spring.datasource.generateUniqueName=false",
        "spring.datasource.name=jdbc"
})
public class MSSQLPooledTransactionalIT extends PooledTransactionalIT implements MSSQLContainerConfiguration {
}

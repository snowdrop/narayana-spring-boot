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

package dev.snowdrop.boot.narayana.pooled;

import dev.snowdrop.boot.narayana.app.TestApplication;
import dev.snowdrop.boot.narayana.generic.GenericRecoveryIT;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@SpringBootTest(classes = TestApplication.class, properties = {
    "narayana.messaginghub.enabled=true",
    "narayana.messaginghub.name=jms",
    "spring.datasource.generateUniqueName=false",
    "spring.datasource.name=jdbc"
})
@EnableAutoConfiguration
public class PooledRecoveryIT extends GenericRecoveryIT {
}

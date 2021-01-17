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

package me.snowdrop.boot.narayana.lrco;

import java.util.List;

import me.snowdrop.boot.narayana.app.Entry;
import me.snowdrop.boot.narayana.app.TestApplication;
import me.snowdrop.boot.narayana.generic.GenericRecoveryIT;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class, properties = {"narayana.dbcp.enabled=true", "narayana.messaginghub.enabled=true", "narayana.lrco.enabled=true"})
public class LrcoPooledRecoveryIT extends GenericRecoveryIT {

    @Override
    protected void assertEntriesAfterCrash(List<Entry> entries) {
        // LRCO resource is committed last, while others get prepared only.
        assertThat(entries)
                .as("Test entry should exist because resource was committed before crash")
                .hasSize(1);
    }
}

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

package dev.snowdrop.narayana.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateAndListUser() throws InterruptedException {
        String name = "Little Finger";
        this.userService.create(name);
        assertThat(this.userService.getAll()).containsOnlyOnce(name);
    }

    @Test
    void shouldNotCreateDuplicateUsers() {
        String name = "King Slayer";
        this.userService.create(name);
        try {
            this.userService.create(name);
            fail("Transaction rollback was expected");
        } catch (Throwable ignored) {
        }
        assertThat(this.userService.getAll()).containsOnlyOnce(name);
    }
}

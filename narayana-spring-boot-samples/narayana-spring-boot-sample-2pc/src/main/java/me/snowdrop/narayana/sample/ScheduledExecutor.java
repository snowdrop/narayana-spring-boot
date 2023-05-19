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

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledExecutor {

    private final UserService userService;

    private final RandomNameService randomNameService;

    public ScheduledExecutor(UserService userService, RandomNameService randomNameService) {
        this.userService = userService;
        this.randomNameService = randomNameService;
    }

    /**
     * Periodically attempt to create a new user and log the success in a JMS logger. This is a transactional operation
     * and both JMS message and database update will either succeed or be canceled.
     * <p>
     * Our database only accepts one user with a specific name. Therefore, because we use a {@link RandomNameService}
     * which has a limited supply of unique names, create operation will start failing eventually, demonstrating a
     * transaction rollback.
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void createNewUser() {
        String name = this.randomNameService.getRandomName();
        System.out.println("Executor ---> Attempting to create a user named " + name);
        try {
            this.userService.create(name);
        } catch (Throwable t) {
            System.out.println("Executor ---> Failed to create a user named " + name + ". Transaction will rollback");
            throw t;
        }
    }

    /**
     * Periodically print a list of users that have been stored in a database so far.
     */
    @Scheduled(fixedRate = 10000)
    public void listUsers() {
        List<String> names = this.userService.getAll();
        if (names.isEmpty()) {
            System.out.println("Executor ---> Users database is still empty");
        } else {
            System.out.println("Executor ---> Current users: " + String.join(", ", names));
        }
    }
}

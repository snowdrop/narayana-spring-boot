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

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    private final JmsTemplate jmsTemplate;

    public UserService(JdbcTemplate jdbcTemplate, JmsTemplate jmsTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jmsTemplate = jmsTemplate;
    }

    @Transactional
    public void create(String name) {
        // Send a message before the update to demonstrate rollback when update fails.
        this.jmsTemplate.convertAndSend(JmsLogger.LOGGER_QUEUE, "Created a new user " + name);
        this.jdbcTemplate.update("insert into USERS(name) values(?)", name);
    }

    public List<String> getAll() {
        return this.jdbcTemplate.queryForList("select NAME from USERS", String.class);
    }
}

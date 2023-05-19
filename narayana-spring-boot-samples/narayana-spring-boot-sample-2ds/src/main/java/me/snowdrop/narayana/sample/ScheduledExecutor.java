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

import java.util.Random;

import javax.sql.DataSource;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledExecutor {

    private final JdbcTemplate ds1;
    private final JdbcTemplate ds2;
    private final Random random;

    public ScheduledExecutor(@Qualifier("ds1") DataSource ds1, @Qualifier("ds2") DataSource ds2) {
        this.ds1 = new JdbcTemplate(ds1);
        this.ds2 = new JdbcTemplate(ds2);
        this.random = new Random();
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void insertData() {
        int value = this.random.nextInt();
        System.out.println("Insert random value '" + value + "' into 2 databases");
        this.ds1.update("insert into dummy values (?)", value);
        this.ds2.update("insert into dummy values (?)", value);
    }
}

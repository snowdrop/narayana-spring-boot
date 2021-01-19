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

package me.snowdrop.boot.narayana.autoconfigure;

import java.util.Properties;

import javax.sql.XADataSource;

import me.snowdrop.boot.narayana.core.jdbc.lrco.LrcoXADataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class NarayanaLrcoXADataSourceAutoConfigurationIT {

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void closeContext() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    void lrcoXADataSourceShouldBeLoaded() {
        Properties properties = new Properties();
        properties.put("narayana.dbcp.enabled", "true");
        properties.put("narayana.lrco.enabled", "true");
        PropertiesPropertySource propertySource = new PropertiesPropertySource("test", properties);

        this.context = new AnnotationConfigApplicationContext();
        this.context.getEnvironment().getPropertySources().addFirst(propertySource);
        this.context.register(
                NarayanaConfiguration.class,
                NarayanaLrcoXADataSourceAutoConfiguration.class);
        this.context.refresh();

        XADataSource xaDataSource = this.context.getBean(XADataSource.class);
        assertThat(xaDataSource).isInstanceOf(LrcoXADataSource.class);
    }
}

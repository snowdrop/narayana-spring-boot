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

package me.snowdrop.boot.narayana.app;

import java.util.List;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Service
public class EntriesService {

    private final Logger logger = LoggerFactory.getLogger(EntriesService.class);

    private final EntriesRepository entriesRepository;

    public EntriesService(EntriesRepository entriesRepository) {
        this.entriesRepository = entriesRepository;
    }

    @Transactional
    public Entry createEntry(String value) {
        this.logger.info("Creating entry '{}'", value);
        return this.entriesRepository.save(new Entry(value));
    }

    public List<Entry> getEntries() {
        List<Entry> entries = this.entriesRepository.findAll();
        this.logger.info("Returning entries '{}'", entries);
        return entries;
    }

    public void clearEntries() {
        this.entriesRepository.deleteAll();
    }

}

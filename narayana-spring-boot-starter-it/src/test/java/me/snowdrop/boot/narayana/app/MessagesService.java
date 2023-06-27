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

import java.util.LinkedList;
import java.util.List;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@Service
public class MessagesService {

    public final static String QUEUE_NAME = "test-messages";

    private final Logger logger = LoggerFactory.getLogger(MessagesService.class);

    private final List<String> receivedMessages = new LinkedList<>();

    private final JmsTemplate jmsTemplate;

    public MessagesService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Transactional
    public void sendMessage(String message) {
        this.logger.info("Sending message '{}' to '{}' queue", message, QUEUE_NAME);
        this.jmsTemplate.convertAndSend(QUEUE_NAME, message);
    }

    public List<String> getReceivedMessages() {
        this.logger.info("Returning received messages '{}'", this.receivedMessages);
        return this.receivedMessages;
    }

    public void clearReceivedMessages() {
        this.receivedMessages.clear();
    }

    @JmsListener(destination = QUEUE_NAME)
    public void onMessage(String message) {
        this.logger.info("Received message '{}'", message);
        this.receivedMessages.add(message);
    }
}

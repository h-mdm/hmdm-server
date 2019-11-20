/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.event;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.util.BackgroundTaskRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>A service to be used for establishing the communication </p>
 *
 * @author isv
 */
@Singleton
public class EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    /**
     * <p>A mapping from the types of event objects and respective listeners for the events of those types.</p>
     */
    private final ConcurrentMap<EventType, List<EventListener<? extends Event>>> eventListeners;

    /**
     * <p>A runner for the tasks notifying listeners on new events.</p>
     */
    private final BackgroundTaskRunnerService taskRunner;

    /**
     * <p>Constructs new <code>EventService</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public EventService(BackgroundTaskRunnerService taskRunner) {
        this.taskRunner = taskRunner;
        final ConcurrentMap<EventType, List<EventListener<? extends Event>>> tmp = new ConcurrentHashMap<>();
        for (EventType eventType : EventType.values()) {
            tmp.put(eventType, new CopyOnWriteArrayList<>());
        }
        this.eventListeners = tmp;
    }

    /**
     * <p>Registers specified listeners for receiving fired events.</p>
     *
     * @param listener a listener to be registered to service.
     */
    public void addEventListener(@NotNull EventListener<? extends Event> listener) {
        this.eventListeners.get(listener.getSupportedEventType()).add(listener);
    }

    /**
     * <p>Notifies the intended listeners on new event.</p>
     *
     * @param event a new fired event to notify the respective listeners on.
     */
    @SuppressWarnings("unchecked")
    public void fireEvent(final Event event) {
        try {
            final List<EventListener<? extends Event>> eventListeners = this.eventListeners.get(event.getType());
            eventListeners.forEach(listener -> {
                final HandleEventTask task = new HandleEventTask(event, listener);
                this.taskRunner.submitTask(task);
            });
        } catch (Exception e) {
            logger.error("Unexpected error when firing event: {}", event, e);
        }
    }

}

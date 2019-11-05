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

/**
 * <p>A task to be run in background thread for notifying the listener on event.</p>
 *
 * @author isv
 */
class HandleEventTask<K extends Event> implements Runnable {

    /**
     * <p>A fired event.</p>
     */
    private final K event;

    /**
     * <p>A listener ot be notified of the event.</p>
     */
    private final EventListener<K> eventListener;

    /**
     * <p>Constructs new <code>HandleEventTask</code> instance. This implementation does nothing.</p>
     */
    HandleEventTask(K event, EventListener<K> eventListener) {
        this.event = event;
        this.eventListener = eventListener;
    }

    /**
     * <p>Runs this task. Notifies the listener on the event fired.</p>
     */
    @Override
    public void run() {
        this.eventListener.onEvent(this.event);
    }
}

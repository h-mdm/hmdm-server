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

package com.hmdm.notification.guice.module;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.hmdm.notification.rest.NotificationResource;
import com.hmdm.rest.filter.AuthFilter;

/**
 * <p>A <code>Guice</code> module for <code>Notification API</code> sub-system.</p>
 *
 * @author isv
 */
public class NotificationRestModule extends ServletModule {


    /**
     * <p>Constructs new <code>NotificationRestModule</code> instance. This implementation does nothing.</p>
     */
    public NotificationRestModule() {
    }

    /**
     * <p>Configures the resources for <code>Plugin Platform</code>.</p>
     */
    protected void configureServlets() {
        this.filter("/rest/notification/private/*").through(AuthFilter.class);
        this.bind(NotificationResource.class);
    }

}

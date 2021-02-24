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

package com.hmdm.rest.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.ApplicationType;
import com.hmdm.persistence.domain.Configuration;

import java.util.List;

/**
 * <p>An interface for the application records included into response sent to device in response to request for
 * configuration synchronization.</p>
 *
 * @author isv
 */
public interface SyncApplicationInt {
    String getIcon();

    String getName();

    String getPkg();

    String getVersion();

    String getUrl();

    Integer getId();

    Boolean getShowIcon();

    Boolean getUseKiosk();

    Boolean isRemove();

    Boolean isSystem();

    Boolean isRunAfterInstall();

    Boolean isRunAtBoot();

    Boolean isSkipVersion();

    String getIconText();

    ApplicationType getType();

    Integer getScreenOrder();

    Integer getKeyCode();

    Boolean getBottom();
}

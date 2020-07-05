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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.Application;
import com.hmdm.persistence.domain.ApplicationType;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;

/**
 * <p>A DTO carrying the data for a single application included into response to request from device for configuration
 * synchronization.</p>
 *
 * @author isv
 */
@ApiModel(description = "A specification of a single application available for usage on mobile device")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncApplication implements SyncApplicationInt {

    @JsonIgnore
    private final Application wrapped;

    /**
     * <p>Constructs new <code>SyncApplication</code> instance. This implementation does nothing.</p>
     */
    public SyncApplication(@NotNull Application wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getIcon() {
        return wrapped.getIcon();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public String getPkg() {
        return wrapped.getPkg();
    }

    @Override
    public String getVersion() {
        return wrapped.getVersion();
    }

    @Override
    public String getUrl() {
        return wrapped.getUrl();
    }

    @Override
    public Integer getId() {
        return wrapped.getId();
    }

    @Override
    public boolean getShowIcon() {
        return wrapped.getShowIcon();
    }

    @Override
    @Deprecated
    public boolean isRemove() {
        return wrapped.isRemove();
    }

    @Override
    public boolean isSystem() {
        return wrapped.isSystem();
    }

    @Override
    public boolean isRunAfterInstall() {
        return wrapped.isRunAfterInstall();
    }

    @Override
    public boolean isRunAtBoot() {
        return wrapped.isRunAtBoot();
    }

    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Boolean isSkipVersion() {
        return wrapped.isSkipVersion();
    }

    @Override
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getIconText() {
        return wrapped.getIconText();
    }

    @Override
    public ApplicationType getType() {
        return wrapped.getType();
    }

    @Override
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Integer getScreenOrder() {
        return wrapped.getScreenOrder();
    }
}

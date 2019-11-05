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

package com.hmdm.rest.json.view.devicelist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.persistence.domain.Application;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A wrapper around the {@link Application} object providing the view suitable for the <code>Device List</code> view
 * of server application. The wrapped application object represents an application installed on device.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(value = {"application"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "A specification of a single application installed and used on mobile device")
public class DeviceApplicationView implements Serializable {

    private static final long serialVersionUID = 1086525298150378152L;
    
    /**
     * <p>A wrapped application object.</p>
     */
    private final Application application;

    /**
     * <p>Constructs new <code>DeviceApplicationView</code> instance. This implementation does nothing.</p>
     */
    DeviceApplicationView(Application application) {
        this.application = application;
    }

    @ApiModelProperty("A package ID of application")
    public String getPkg() {
        return application.getPkg();
    }

    @ApiModelProperty("A version of application")
    public String getVersion() {
        return application.getVersion();
    }
}

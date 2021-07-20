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
 * of server application.</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(value = {"application"}, ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "A specification of a single application available for usage on mobile device")
public class ApplicationView implements Serializable {

    private static final long serialVersionUID = 2154319093765210298L;
    
    /**
     * <p>A wrapped application object.</p>
     */
    private final Application application;

    /**
     * <p>Constructs new <code>ApplicationView</code> instance. This implementation does nothing.</p>
     */
    ApplicationView(Application application) {
        this.application = application;
    }

    @ApiModelProperty("A package ID of application")
    public String getPkg() {
        return this.application.getPkg();
    }

    @ApiModelProperty("A version of application")
    public String getVersion() {
        return this.application.getVersion();
    }

    @ApiModelProperty("An URL for application package")
    public String getUrl() {
        if (this.application.getUrl() != null) {
            return this.application.getUrl();
        } else {
            // URL is used just to check the application status
            // So for split APKs we return the first non-null URL
            // If an app is not installed but has at least one APK URL, we treat this as an error
            if (this.application.getUrlArm64() != null) {
                return this.application.getUrlArm64();
            }
            return this.application.getUrlArmeabi();
        }
    }

    @ApiModelProperty("An application ID")
    public Integer getId() {
        return this.application.getId();
    }

    @ApiModelProperty("A flag indicating if application is used in device configuration")
    public boolean isSelected() {
        return this.application.isSelected();
    }

    @ApiModelProperty("A flag indicating if application version shouldnt be checked")
    public boolean isSkipVersion() {
        return this.application.isSkipVersion();
    }

    // A helper property to indicate the action required to be performed by mobile device
    // in regard to application installation
    // 0 - do not install and hide if installed
    // 1 - install
    // 2 - do not install and remove if installed
    @ApiModelProperty(value = "The action required to be performed by mobile device", allowableValues = "0,1,2")
    public int getAction() {
        return this.application.getAction();
    }

    @ApiModelProperty("A name of application")
    public String getName() {
        return this.application.getName();
    }
}

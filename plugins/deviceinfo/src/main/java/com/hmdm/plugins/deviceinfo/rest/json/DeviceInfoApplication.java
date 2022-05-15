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

package com.hmdm.plugins.deviceinfo.rest.json;

import com.hmdm.persistence.domain.Application;
import com.hmdm.util.ApplicationUtil;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A DTO carrying the details for a single application which is already installed or must be installed on device.</p>
 *
 * @author isv
 */
public class DeviceInfoApplication implements Serializable {

    private static final long serialVersionUID = -2704172120435523175L;
    
    @ApiModelProperty("A name of the application")
    private String applicationName;

    @ApiModelProperty("A package ID of the application")
    private String applicationPkg;

    @ApiModelProperty("A number of application version already installed on device")
    private String versionInstalled;

    @ApiModelProperty("A number of application version which is required to be installed on device")
    private String versionRequired;

    /**
     * <p>Constructs new <code>DeviceInfoApplication</code> instance. This implementation does nothing.</p>
     */
    public DeviceInfoApplication() {
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationPkg() {
        return applicationPkg;
    }

    public void setApplicationPkg(String applicationPkg) {
        this.applicationPkg = applicationPkg;
    }

    public String getVersionInstalled() {
        return versionInstalled;
    }

    public void setVersionInstalled(String versionInstalled) {
        this.versionInstalled = versionInstalled;
    }

    public String getVersionRequired() {
        return versionRequired;
    }

    public void setVersionRequired(String versionRequired) {
        this.versionRequired = versionRequired;
    }

    /**
     * <p>Checks if application version already installed on device is the same as required one to be installed on
     * device.</p>
     *
     * @return <code>true</code> if installed application version matches the required application version;
     *         <code>false</code> otherwise.
     */
    public boolean isVersionValid() {
        final String v1 = ApplicationUtil.normalizeVersion(this.versionInstalled);
        final String v2 = ApplicationUtil.normalizeVersion(this.versionRequired);

        return v1.equals(v2) || v2.equals("0");
    }

    @Override
    public String toString() {
        return "DeviceInfoApplication{" +
                "applicationName='" + applicationName + '\'' +
                ", applicationPkg='" + applicationPkg + '\'' +
                ", versionInstalled='" + versionInstalled + '\'' +
                ", versionRequired='" + versionRequired + '\'' +
                '}';
    }
}

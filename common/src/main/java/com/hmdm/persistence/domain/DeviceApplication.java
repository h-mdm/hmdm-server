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

package com.hmdm.persistence.domain;

import java.io.Serializable;

/**
 * <p>A single application reported as installed on device.</p>
 *
 * @author isv
 */
public class DeviceApplication implements Serializable {

    private static final long serialVersionUID = 9105449738375462219L;
    
    /**
     * <p>A package ID for application.</p>
     */
    private String pkg;

    /**
     * <p>An application version.</p>
     */
    private String version;

    /**
     * <p>An application name.</p>
     */
    private String name;

    /**
     * <p>Constructs new <code>DeviceApplication</code> instance. This implementation does nothing.</p>
     */
    public DeviceApplication() {
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "DeviceApplication{" +
                "pkg='" + pkg + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

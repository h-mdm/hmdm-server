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

import java.io.Serializable;

/**
 * <p>$</p>
 *
 * @author isv
 */
public class APKFileDetails implements Serializable {

    private static final long serialVersionUID = 2800294500441884483L;

    /**
     * <p>An application package ID.</p>
     */
    private String pkg;

    /**
     * <p>An application version number.</p>
     */
    private String version;

    /**
     * <p>An application version code.</p>
     */
    private int versionCode;

    /**
     * <p>CPU architecture (null = universal, armeabi or arm64).</p>
     */
    private String arch;

    /**
     * <p>Constructs new <code>APKFileDetails</code> instance. This implementation does nothing.</p>
     */
    public APKFileDetails() {
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

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    @Override
    public String toString() {
        return "APKFileDetails{" +
                "pkg='" + pkg + '\'' +
                ", version='" + version + '\'' +
                ", versionCode=" + versionCode +
                ", arch='" + arch + '\'' +
                '}';
    }
}

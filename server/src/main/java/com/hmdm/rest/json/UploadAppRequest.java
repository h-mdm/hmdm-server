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
 * <p>A DTO representing a request for uploading new application to server.</p>
 *
 * @author isv
 */
public class UploadAppRequest implements Serializable {

    private static final long serialVersionUID = 514589555301053784L;
    
    // Application data
    private String localPath;
    private String name;
    private String fileName;
    private String version;
    private String pkg;
    private boolean showIcon;
    private boolean useKiosk;
    private boolean runAfterInstall;
    private boolean runAtBoot;
    private boolean system;

    // Request related data
    private String deviceId;
    private String hash;

    /**
     * <p>Constructs new <code>UploadAppRequest</code> instance. This implementation does nothing.</p>
     */
    public UploadAppRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public boolean isShowIcon() {
        return showIcon;
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }

    public boolean isUseKiosk() {
        return useKiosk;
    }

    public void setUseKiosk(boolean useKiosk) {
        this.useKiosk = useKiosk;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isRunAfterInstall() {
        return runAfterInstall;
    }

    public void setRunAfterInstall(boolean runAfterInstall) {
        this.runAfterInstall = runAfterInstall;
    }

    public boolean isRunAtBoot() {
        return runAtBoot;
    }

    public void setRunAtBoot(boolean runAtBoot) {
        this.runAtBoot = runAtBoot;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    @Override
    public String toString() {
        return "UploadAppRequest{" +
                "localPath='" + localPath + '\'' +
                ", name='" + name + '\'' +
                ", fileName='" + fileName + '\'' +
                ", version='" + version + '\'' +
                ", pkg='" + pkg + '\'' +
                ", showIcon=" + showIcon +
                ", useKiosk=" + useKiosk +
                ", runAfterInstall=" + runAfterInstall +
                ", runAtBoot=" + runAtBoot +
                ", system=" + system +
                ", deviceId='" + deviceId + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}

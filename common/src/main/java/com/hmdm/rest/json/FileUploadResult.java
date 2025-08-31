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

import com.hmdm.persistence.domain.Application;

import java.io.Serializable;

/**
 * <p>$</p>
 *
 * @author isv
 */
public class FileUploadResult implements Serializable {

    private static final long serialVersionUID = 5341039993951240166L;

    /**
     * <p>A path to uploaded file on the server.</p>
     */
    private String serverPath;

    /**
     * <p>A details on uploaded APK-file.</p>
     */
    private APKFileDetails fileDetails;

    /**
     * <p>An existing application which the uploaded file is mapped to based on package ID.</p>
     */
    private Application application;

    /**
     * <p>True if both CPU architectures are uploaded or for universal APK.</p>
     */
    private Boolean complete;

    /**
     * <p>True if version already exists.</p>
     */
    private Boolean exists;

    /**
     * <p>Returns the name of the uploaded file</p>
     */
    private String name;

    /**
     * <p>Constructs new <code>FileUploadResult</code> instance. This implementation does nothing.</p>
     */
    public FileUploadResult() {
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    public void setFileDetails(APKFileDetails fileDetails) {
        this.fileDetails = fileDetails;
    }

    public APKFileDetails getFileDetails() {
        return fileDetails;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Application getApplication() {
        return application;
    }

    public Boolean getComplete() {
        return complete;
    }

    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public Boolean getExists() {
        return exists;
    }

    public void setExists(Boolean exists) {
        this.exists = exists;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FileUploadResult{" +
                "serverPath='" + serverPath + '\'' +
                ", fileDetails=" + fileDetails +
                ", application=" + application +
                ", complete=" + complete +
                ", exists=" + exists +
                ", name='" + name + '\'' +
                '}';
    }
}

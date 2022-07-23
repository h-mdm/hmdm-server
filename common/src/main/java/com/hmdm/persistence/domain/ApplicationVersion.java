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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@ApiModel(description = "A specification of a single application version installed and used on mobile device")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationVersion implements Serializable {

    private static final long serialVersionUID = 3429103100994111887L;
    @ApiModelProperty("An application version ID")
    private Integer id;

    @ApiModelProperty("An application ID")
    private Integer applicationId;

    @ApiModelProperty("A version of application")
    private String version;

    @ApiModelProperty("Version code")
    private int versionCode;

    @ApiModelProperty("An URL for application package")
    private String url;

    @ApiModelProperty("Has the APK native code, i.e. is split into two APKs")
    private boolean split;

    @ApiModelProperty("An URL for armeabi APK")
    private String urlArmeabi;

    @ApiModelProperty("An URL for arm64 APK")
    private String urlArm64;

    @ApiModelProperty(hidden = true)
    private boolean deletionProhibited;

    @ApiModelProperty(hidden = true)
    private boolean commonApplication;

    @ApiModelProperty(hidden = true)
    private boolean system;

    @ApiModelProperty(hidden = true)
    private ApplicationType type;

    @ApiModelProperty(hidden = true)
    private String apkHash;

    /**
     * <p>A path to uploaded file to link this application to when adding an application.</p>
     */
    @ApiModelProperty(hidden = true)
    private String filePath;

    /**
     * <p>Constructs new <code>ApplicationVersion</code> instance. This implementation does nothing.</p>
     */
    public ApplicationVersion() {
    }

    /**
     * <p>Constructs new <code>ApplicationVersion</code> instance. This implementation does nothing.</p>
     */
    public ApplicationVersion(Application application) {
        this.applicationId = application.getId();
        this.version = application.getVersion();
        this.versionCode = application.getVersionCode();
        if (application.getArch() == null) {
            this.url = application.getUrl();
        } else if (application.getArch().equals(Application.ARCH_ARMEABI)) {
            this.split = true;
            this.urlArmeabi = application.getUrl();
        } else if (application.getArch().equals(Application.ARCH_ARM64)) {
            this.split = true;
            this.urlArm64 = application.getUrl();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public String getUrlArmeabi() {
        return urlArmeabi;
    }

    public void setUrlArmeabi(String urlArmeabi) {
        this.urlArmeabi = urlArmeabi;
    }

    public String getUrlArm64() {
        return urlArm64;
    }

    public void setUrlArm64(String urlArm64) {
        this.urlArm64 = urlArm64;
    }

    public boolean isDeletionProhibited() {
        return deletionProhibited;
    }

    public void setDeletionProhibited(boolean deletionProhibited) {
        this.deletionProhibited = deletionProhibited;
    }

    public boolean isCommonApplication() {
        return commonApplication;
    }

    public void setCommonApplication(boolean commonApplication) {
        this.commonApplication = commonApplication;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getApkHash() {
        return apkHash;
    }

    public void setApkHash(String apkHash) {
        this.apkHash = apkHash;
    }

    public ApplicationType getType() {
        return type;
    }

    public void setType(ApplicationType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ApplicationVersion{" +
                "id=" + id +
                ", applicationId=" + applicationId +
                ", version='" + version + '\'' +
                ", versionCode=" + versionCode +
                ", system='" + system + '\'' +
                ", url='" + url + '\'' +
                ", apkHash='" + apkHash + '\'' +
                ", deletionProhibited='" + deletionProhibited + '\'' +
                ", commonApplication='" + commonApplication + '\'' +
                ", filePath='" + filePath + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

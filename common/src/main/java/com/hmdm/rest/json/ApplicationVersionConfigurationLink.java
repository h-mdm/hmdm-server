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
import com.hmdm.persistence.domain.CustomerData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>$</p>
 *
 * @author isv
 */
@ApiModel(description = "A link between the single application version and single configuration")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApplicationVersionConfigurationLink implements CustomerData {

    @ApiModelProperty(value = "An ID of a link between the application and configuration. " +
            "May be null if those are not linked", required = false)
    private Integer id;
    @ApiModelProperty("An ID of a customer account which both the application and configuration belong to")
    private int customerId;
    @ApiModelProperty("An ID of a configuration")
    private int configurationId;
    @ApiModelProperty("A name of a configuration")
    private String configurationName;
    @ApiModelProperty("An ID of an application")
    private int applicationId;
    @ApiModelProperty("A name of an application")
    private String applicationName;
    @ApiModelProperty("An ID of an application version")
    private int applicationVersionId;
    @ApiModelProperty("A version text of an application version")
    private int versionText;
    @ApiModelProperty("A flag indicating if icon is to be shown on mobile device")
    private Boolean showIcon;
    @ApiModelProperty("Order of applications on the screen")
    private Integer screenOrder;
    @ApiModelProperty("Key code for fast app start")
    private Integer keyCode;
    @ApiModelProperty("A flag indicating if application must be displayed at the bottom of the launcher")
    private boolean bottom;

    // A helper property to indicate the action required to be performed by mobile device
    // in regard to application installation
    // 0 - do not install and hide if installed
    // 1 - install
    // 2 - do not install and remove if installed
    @ApiModelProperty(
            value = "An action required to be performed by mobile device in regard to application installation",
            allowableValues = "0,1,2"
    )
    private int action;
    @ApiModelProperty(value = "A flag indicating that application is to be removed from the application")
    private boolean remove;


    /**
     * <p>Constructs new <code>ApplicationVersionConfigurationLink</code> instance. This implementation does nothing.</p>
     */
    public ApplicationVersionConfigurationLink() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }


    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public int getApplicationVersionId() {
        return applicationVersionId;
    }

    public void setApplicationVersionId(int applicationVersionId) {
        this.applicationVersionId = applicationVersionId;
    }

    public int getVersionText() {
        return versionText;
    }

    public void setVersionText(int versionText) {
        this.versionText = versionText;
    }

    public Boolean getShowIcon() {
        return showIcon;
    }

    public void setShowIcon(Boolean showIcon) {
        this.showIcon = showIcon;
    }

    public Integer getScreenOrder() {
        return screenOrder;
    }

    public void setScreenOrder(Integer screenOrder) {
        this.screenOrder = screenOrder;
    }

    public Integer getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(Integer keyCode) {
        this.keyCode = keyCode;
    }

    public boolean getBottom() {
        return bottom;
    }

    public void setBottom(boolean bottom) {
        this.bottom = bottom;
    }

    @Override
    public String toString() {
        return "ApplicationVersionConfigurationLink{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", configurationId=" + configurationId +
                ", configurationName='" + configurationName + '\'' +
                ", applicationId=" + applicationId +
                ", applicationName='" + applicationName + '\'' +
                ", applicationVersionId=" + applicationVersionId +
                ", versionText=" + versionText +
                ", action=" + action +
                ", remove=" + remove +
                ", showIcon=" + showIcon +
                ", screenOrder=" + screenOrder +
                ", keyCode=" + keyCode +
                ", bottom=" + bottom +
                '}';
    }
}

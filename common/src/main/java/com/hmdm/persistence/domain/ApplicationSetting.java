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

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@ApiModel(description = "A single setting for an application installed and used on mobile device")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationSetting implements Serializable {

    private static final long serialVersionUID = -7840348027518868191L;

    @ApiModelProperty("An ID of a setting record")
    private Integer id;

    @ApiModelProperty(value = "An ID of application", required = true)
    private Integer applicationId;

    @ApiModelProperty(value = "A name of the setting", required = true)
    private String name;

    @ApiModelProperty(value = "A type of the application setting", required = true)
    private ApplicationSettingType type;

    @ApiModelProperty("A value of the setting")
    private String value;

    @ApiModelProperty("A comment on the setting")
    private String comment;

    @ApiModelProperty("A timestamp of the last update of the setting")
    private long lastUpdate;

    @ApiModelProperty(value = "A flag indicating if setting can not be modified on device", required = true)
    private boolean readonly;

    @ApiModelProperty(value = "An ID of the external object (device, configuration) which settings belong to", required = true)
    private Integer extRefId;

    @ApiModelProperty(hidden = true)
    private String applicationPkg;

    @ApiModelProperty(hidden = true)
    private String applicationName;

    // A name of the external object (device, configuration) which settings belong to
    @ApiModelProperty(hidden = true)
    private String extRefName;

    /**
     * <p>Constructs new <code>ApplicationSetting</code> instance. This implementation does nothing.</p>
     */
    public ApplicationSetting() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueForDevice(Device device) {
        return value
                .replaceAll("%NUMBER%", device.getNumber())
                .replaceAll("%IMEI%", device.getImei())
                .replaceAll("%PHONE%", device.getPhone())
                .replaceAll("%DESCRIPTION%", device.getDescription())
                .replaceAll("%CUSTOM1%", device.getCustom1())
                .replaceAll("%CUSTOM2%", device.getCustom2())
                .replaceAll("%CUSTOM3%", device.getCustom3());
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public Integer getExtRefId() {
        return extRefId;
    }

    public void setExtRefId(Integer extRefId) {
        this.extRefId = extRefId;
    }

    public String getApplicationPkg() {
        return applicationPkg;
    }

    public void setApplicationPkg(String applicationPkg) {
        this.applicationPkg = applicationPkg;
    }

    public String getExtRefName() {
        return extRefName;
    }

    public void setExtRefName(String extRefName) {
        this.extRefName = extRefName;
    }

    public ApplicationSettingType getType() {
        return type;
    }

    public void setType(ApplicationSettingType type) {
        this.type = type;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String toString() {
        return "ApplicationSetting{" +
                "id=" + id +
                ", applicationId=" + applicationId +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", comment='" + comment + '\'' +
                ", readonly=" + readonly +
                ", extRefId=" + extRefId +
                ", type=" + type +
                ", applicationPkg='" + applicationPkg + '\'' +
                ", extRefName='" + extRefName + '\'' +
                ", lastUpdate='" + lastUpdate + '\'' +
                '}';
    }
}

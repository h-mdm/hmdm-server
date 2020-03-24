package com.hmdm.plugins.messaging.rest.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>A DTO object for the message sending command.</p>
 *
 * @author seva
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendRequest {
    private String scope;
    private String deviceNumber;
    private Integer groupId;
    private Integer configurationId;
    private String message;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Integer configurationId) {
        this.configurationId = configurationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

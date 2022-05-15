package com.hmdm.plugins.push.rest.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>A DTO object for the Push message sending command.</p>
 *
 * @author seva
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushSendRequest {
    private String scope;
    private String deviceNumber;
    private Integer groupId;
    private Integer configurationId;
    private String messageType;
    private String payload;

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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}

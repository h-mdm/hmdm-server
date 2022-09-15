package com.hmdm.notification;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttEnvelope {
    private String address;
    private MqttMessage message;

    public MqttEnvelope() {}

    public MqttEnvelope(String address, MqttMessage message) {
        this.address = address;
        this.message = message;
    }

    public MqttMessage getMessage() {
        return message;
    }

    public void setMessage(MqttMessage message) {
        this.message = message;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

package com.hmdm.notification;

import com.hivemq.client.mqtt.datatypes.MqttQos;

public class MqttEnvelope {
    private String address;
    private byte[] payload;
    private MqttQos qos;

    public MqttEnvelope() {}

    public MqttEnvelope(String address, byte[] payload, MqttQos qos) {
        this.address = address;
        this.payload = payload;
        this.qos = qos;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public MqttQos getQos() {
        return qos;
    }

    public void setQos(MqttQos qos) {
        this.qos = qos;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

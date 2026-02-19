package com.hmdm.notification;

public class MqttEnvelope {
    private String address;
    private byte[] payload;
    private int qos;

    public MqttEnvelope() {}

    public MqttEnvelope(String address, byte[] payload, int qos) {
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

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

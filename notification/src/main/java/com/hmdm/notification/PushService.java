package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hmdm.notification.persistence.domain.PushMessage;
import com.hmdm.notification.persistence.mapper.NotificationMapper;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.DeviceDAO;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.persistence.domain.Device;
import org.mybatis.guice.transactional.Transactional;

import java.util.List;

@Singleton
public class PushService {

    private final PushSender pushSenderMqtt;
    private final PushSender pushSenderPolling;
    private final ConfigurationDAO configurationDAO;
    private final DeviceDAO deviceDAO;

    @Inject
    public PushService(@Named("MQTT") PushSender pushSenderMqtt, @Named("Polling") PushSender pushSenderPolling,
                       ConfigurationDAO configurationDAO, DeviceDAO deviceDAO) {
        this.pushSenderMqtt = pushSenderMqtt;
        this.pushSenderPolling = pushSenderPolling;
        this.configurationDAO = configurationDAO;
        this.deviceDAO = deviceDAO;
    }

    // Use both ways to send a message, because the decision how to receive messages is done on the device (configuration)
    public int send(PushMessage message) {
        pushSenderMqtt.send(message);
        return pushSenderPolling.send(message);
    }

    /**
     * <p>Sends the messages on configuration update for the devices related to specified configuration.</p>
     *
     * @param configurationId an ID of updated configuration.
     */
    @Transactional
    public void notifyDevicesOnUpdate(Integer configurationId) {
        final Configuration configuration = this.configurationDAO.getConfigurationById(configurationId);
        if (configuration != null) {
            final List<Device> devices
                    = this.deviceDAO.getDeviceIdsByConfigurationId(configurationId);
            devices.forEach(device -> {
                PushMessage message = new PushMessage();
                message.setDeviceId(device.getId());
                message.setMessageType(PushMessage.TYPE_CONFIG_UPDATED);

                this.send(message);
            });
        }
    }

    /**
     * <p>Sends the message on application settings update to specified device.</p>
     *
     * @param deviceId an ID of device to be notified.
     */
    @Transactional
    public void notifyDeviceOnSettingUpdate(Integer deviceId) {
        sendSimpleMessage(deviceId, PushMessage.TYPE_CONFIG_UPDATED);
    }

    /**
     * <p>Sends the message on application settings update to specified device.</p>
     *
     * @param deviceId an ID of device to be notified.
     */
    @Transactional
    public void notifyDeviceOnApplicationSettingUpdate(Integer deviceId) {
        sendSimpleMessage(deviceId, PushMessage.TYPE_APP_CONFIG_UPDATED);
    }

    /**
     * <p>Sends the simple message of a certain type to specified device.</p>
     *
     * @param deviceId an ID of device to be notified.
     * @param messageType Message type
     */
    public void sendSimpleMessage(Integer deviceId, String messageType) {
        final Device dbDevice = this.deviceDAO.getDeviceById(deviceId);
        if (dbDevice != null) {
            PushMessage message = new PushMessage();
            message.setDeviceId(dbDevice.getId());
            message.setMessageType(messageType);

            this.send(message);
        }
    }
}

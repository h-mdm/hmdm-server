package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hmdm.notification.persistence.NotificationDAO;
import com.hmdm.notification.persistence.domain.PushMessage;

import javax.servlet.AsyncContext;
import java.util.*;

@Singleton
public class PushSenderPolling implements PushSender {
    private final NotificationDAO notificationDAO;
    private final Map<Integer, DeviceEntry> deviceIdMap = new HashMap<>();
    private final Map<AsyncContext, DeviceEntry> deviceContextMap = new HashMap<>();

    @Inject
    public PushSenderPolling(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    @Override
    public void init() {
        // Do nothing here
    }

    @Override
    public int send(PushMessage message) {
        DeviceEntry deviceEntry = deviceIdMap.get(message.getDeviceId());
        if (deviceEntry == null) {
            // Device is offline,
            return notificationDAO.send(message);
        }
        List<PushMessage> pendingMessages = notificationDAO.getPendingMessagesForDelivery(message.getDeviceId());
        synchronized (deviceEntry.messages) {
            for (PushMessage pending : pendingMessages) {
                deviceEntry.messages.add(pending);
            }
            deviceEntry.messages.add(message);
        }
        deviceEntry.context.complete();
        return 0;
    }

    public void sendPending(int deviceId, List<PushMessage> messages) {
        DeviceEntry deviceEntry = deviceIdMap.get(deviceId);
        if (deviceEntry == null) {
            // We shouldn't be here as this method is called from LongPollingServlet when a device is online
            return;
        }
        synchronized (deviceEntry.messages) {
            for (PushMessage pending : messages) {
                deviceEntry.messages.add(pending);
            }
        }
        deviceEntry.context.complete();

    }

    public List<PushMessage> getPendingMessages(AsyncContext asyncContext) {
        DeviceEntry deviceEntry = deviceContextMap.get(asyncContext);
        List<PushMessage> result = new LinkedList<>();
        if (deviceEntry != null) {
            synchronized (deviceEntry.messages) {
                result.addAll(deviceEntry.messages);
                deviceEntry.messages.clear();
            }
        }
        return result;
    }

    public void register(Integer deviceId, AsyncContext asyncContext) {
        DeviceEntry deviceEntry = new DeviceEntry(deviceId, asyncContext);
        deviceIdMap.put(deviceId, deviceEntry);
        deviceContextMap.put(asyncContext, deviceEntry);
    }

    public void unregister(AsyncContext asyncContext) {
        DeviceEntry deviceEntry = deviceContextMap.get(asyncContext);
        if (deviceEntry != null) {
            deviceIdMap.remove(deviceEntry.deviceId);
            deviceContextMap.remove(asyncContext);
        }
    }

    public class DeviceEntry {
        public Integer deviceId;
        public AsyncContext context;
        public final List<PushMessage> messages = new LinkedList<>();

        public DeviceEntry(Integer deviceId, AsyncContext context) {
            this.deviceId = deviceId;
            this.context = context;
        }
    }
}

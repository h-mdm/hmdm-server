package com.hmdm.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hmdm.notification.persistence.NotificationDAO;
import com.hmdm.notification.persistence.domain.PushMessage;

@Singleton
public class PushSenderPolling implements PushSender {
    private final NotificationDAO notificationDAO;

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
        return notificationDAO.send(message);
    }
}

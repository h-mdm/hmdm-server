package com.hmdm.notification;

import com.hmdm.notification.persistence.domain.PushMessage;

public interface PushSender {
    public void init();
    public int send(PushMessage message);
}

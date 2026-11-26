package org.sky.service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sky.service.service.NotificationService;

/** SMS-уведомления. */
@Slf4j
public class SmsNotification implements NotificationService {
    @Override
    public void sendNotification(String message, String recipient) {
        log.info("SMS -> {}: {}", recipient, message);
    }
}

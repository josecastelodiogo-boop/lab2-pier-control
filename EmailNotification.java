package org.sky.service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.sky.service.service.NotificationService;

/** Email-уведомления. @Slf4j создаёт поле log (вместо System.out.println). */
@Slf4j
public class EmailNotification implements NotificationService {
    @Override
    public void sendNotification(String message, String recipient) {
        log.info("EMAIL -> {}: {}", recipient, message);
    }
}

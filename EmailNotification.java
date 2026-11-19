package org.sky.service.service.impl;

import org.sky.service.service.NotificationService;

public class EmailNotification implements NotificationService {
    @Override
    public void sendNotification(String message, String recipient) {
        System.out.printf("[EMAIL] На адрес <%s> отправлено письмо: %s%n", recipient, message);
    }
}

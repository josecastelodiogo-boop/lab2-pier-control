package org.sky.service.service;

/** Контракт отправки уведомлений. */
@FunctionalInterface
public interface NotificationService {
    void sendNotification(String message, String recipient);
}

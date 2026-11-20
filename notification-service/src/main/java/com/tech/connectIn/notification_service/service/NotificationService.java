package com.tech.connectIn.notification_service.service;

import com.tech.connectIn.notification_service.entity.Notification;

import java.util.List;

public interface NotificationService {
    void addNotification(Notification notification);
    List<Notification> getNotificationsForUser(Long userId);
}

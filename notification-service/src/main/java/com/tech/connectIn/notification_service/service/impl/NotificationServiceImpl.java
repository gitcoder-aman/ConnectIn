package com.tech.connectIn.notification_service.service.impl;

import com.tech.connectIn.notification_service.entity.Notification;
import com.tech.connectIn.notification_service.repository.NotificationRepository;
import com.tech.connectIn.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;


    @Override
    public void addNotification(Notification notification) {
        log.info("adding notification to DB, message:{}",notification.getMessage());
        notification = notificationRepository.save(notification);
    }
}

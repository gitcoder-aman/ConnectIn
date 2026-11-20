package com.tech.connectIn.notification_service.controller;

import com.tech.connectIn.notification_service.entity.Notification;
import com.tech.connectIn.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUserId(@PathVariable Long userId) {
        log.info("Fetching notifications for user with ID: {}", userId);
        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId) {

        Long targetId = headerUserId != null ? headerUserId : paramUserId;
        if (targetId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        log.info("Fetching my notifications for ID: {}", targetId);
        List<Notification> notifications = notificationService.getNotificationsForUser(targetId);
        return ResponseEntity.ok(notifications);
    }
}

package com.lms.lms.controller;

import com.lms.lms.model.Notification;
import com.lms.lms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // مسار جلب الإشعارات غير المقروءة، يعتمد على الـ Token فقط
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnReadNotifications() {
        try {
            return ResponseEntity.ok(notificationService.getMyUnReadNotifications());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // مسار جلب كل الإشعارات، يعتمد على الـ Token فقط
    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        try {
            return ResponseEntity.ok(notificationService.getMyAllNotifications());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // مسار قراءة الإشعار، مؤمّن الآن داخل الـ Service
    @PutMapping("/markAsRead/{notificationId}")
    public ResponseEntity<String> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok("Notification marked as read successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
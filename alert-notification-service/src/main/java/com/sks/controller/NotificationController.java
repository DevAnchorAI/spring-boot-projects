package com.sks.controller;

import com.sks.dto.NotificationRequest;
import com.sks.dto.NotificationResponse;
import com.sks.entity.NotificationStatus;
import com.sks.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable Long id) {
        NotificationResponse response = notificationService.getNotification(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        List<NotificationResponse> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByEmail(@PathVariable String email) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByEmail(email);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByStatus(@PathVariable NotificationStatus status) {
        List<NotificationResponse> notifications = notificationService.getNotificationsByStatus(status);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<String> retryFailedNotifications() {
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok("Retry process started for failed notifications");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}

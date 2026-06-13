package com.sks.service;

import com.sks.dto.NotificationRequest;
import com.sks.dto.NotificationResponse;
import com.sks.entity.Notification;
import com.sks.entity.NotificationStatus;
import com.sks.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {

    private static final int MAX_RETRIES = 3;

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final com.sks.kafka.NotificationProducer notificationProducer;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService, com.sks.kafka.NotificationProducer notificationProducer) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.notificationProducer = notificationProducer;
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
            .subject(request.getSubject())
            .message(request.getMessage())
            .recipientEmail(request.getRecipientEmail())
            .type(request.getType())
            .status(NotificationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .retryCount(0)
            .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Publish to Kafka for async processing
        try {
            notificationProducer.publish(request);
        } catch (Exception ex) {
            log.error("Failed to publish notification to Kafka, falling back to local send: {}", ex.getMessage());
            // fallback to immediate send
            sendNotification(savedNotification);
        }

        return mapToResponse(savedNotification);
    }

    public NotificationResponse getNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        return mapToResponse(notification);
    }

    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<NotificationResponse> getNotificationsByEmail(String email) {
        return notificationRepository.findByRecipientEmail(email)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<NotificationResponse> getNotificationsByStatus(NotificationStatus status) {
        return notificationRepository.findByStatus(status)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public void retryFailedNotifications() {
        List<Notification> failedNotifications =
            notificationRepository.findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, MAX_RETRIES);

        for (Notification notification : failedNotifications) {
            sendNotification(notification);
        }
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    /**
     * Called by local code or by Kafka consumer to perform the actual send and update DB.
     */
    public void processNotification(NotificationRequest request) {
        // try to find an existing PENDING notification for same recipient/subject/message, otherwise create a new DB record
        Notification notification = Notification.builder()
            .subject(request.getSubject())
            .message(request.getMessage())
            .recipientEmail(request.getRecipientEmail())
            .type(request.getType())
            .status(NotificationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .retryCount(0)
            .build();

        Notification savedNotification = notificationRepository.save(notification);
        sendNotification(savedNotification);
    }

    private void sendNotification(Notification notification) {
        boolean sent = emailService.sendEmail(
            notification.getRecipientEmail(),
            notification.getSubject(),
            notification.getMessage()
        );

        if (sent) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } else {
            if (notification.getRetryCount() < MAX_RETRIES) {
                notification.setStatus(NotificationStatus.RETRY);
                notification.setRetryCount(notification.getRetryCount() + 1);
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setFailureReason("Max retries exceeded");
            }
        }

        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .subject(notification.getSubject())
            .message(notification.getMessage())
            .recipientEmail(notification.getRecipientEmail())
            .status(notification.getStatus())
            .type(notification.getType())
            .createdAt(notification.getCreatedAt())
            .sentAt(notification.getSentAt())
            .failureReason(notification.getFailureReason())
            .retryCount(notification.getRetryCount())
            .build();
    }
}

package com.sks.kafka;

import com.sks.entity.Notification;
import com.sks.entity.NotificationStatus;
import com.sks.repository.NotificationRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    private final NotificationRepository notificationRepository;

    private final KafkaTemplate<String, com.sks.dto.NotificationRequest> kafkaTemplate;

    public OutboxPublisher(NotificationRepository notificationRepository,
                           KafkaTemplate<String, com.sks.dto.NotificationRequest> kafkaTemplate) {
        this.notificationRepository = notificationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // poll every 5 seconds
    @Scheduled(fixedDelayString = "5000")
    public void publishPendingNotifications() {
        List<Notification> pending = notificationRepository.findByStatus(NotificationStatus.PENDING);
        for (Notification n : pending) {
            try {
                com.sks.dto.NotificationRequest req = com.sks.dto.NotificationRequest.builder()
                        .subject(n.getSubject())
                        .message(n.getMessage())
                        .recipientEmail(n.getRecipientEmail())
                        .type(n.getType())
                        .build();

                kafkaTemplate.send("notifications", req);
                n.setStatus(NotificationStatus.RETRY);
                n.setRetryCount(n.getRetryCount() == null ? 1 : n.getRetryCount() + 1);
                notificationRepository.save(n);
            } catch (Exception e) {
                // if publishing fails, leave it as PENDING or mark failed later
                n.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(n);
            }
        }
    }
}

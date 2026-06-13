package com.sks.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sks.dto.NotificationRequest;
import com.sks.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "notifications", groupId = "alert-notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(String payload) {
        try {
            NotificationRequest request = objectMapper.readValue(payload, NotificationRequest.class);
            log.info("Consumed notification request from Kafka: {}", request);
            // process - this will create/find DB record and perform the send
            notificationService.processNotification(request);
        } catch (Exception e) {
            log.error("Failed to parse/process notification request: {}", e.getMessage(), e);
        }
    }
}

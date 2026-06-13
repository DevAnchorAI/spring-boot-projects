package com.sks.dto;

import com.sks.entity.NotificationStatus;
import com.sks.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private String subject;
    private String message;
    private String recipientEmail;
    private NotificationStatus status;
    private NotificationType type;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private String failureReason;
    private Integer retryCount;
}


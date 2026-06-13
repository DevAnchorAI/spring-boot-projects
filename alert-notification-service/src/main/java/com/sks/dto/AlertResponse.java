package com.sks.dto;

import com.sks.entity.AlertSeverity;
import com.sks.entity.AlertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {
    private Long id;
    private String title;
    private String description;
    private AlertStatus status;
    private AlertSeverity severity;
    private String recipientEmail;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
}


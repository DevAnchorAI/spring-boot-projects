package com.sks.service;

import com.sks.dto.AlertRequest;
import com.sks.dto.AlertResponse;
import com.sks.entity.Alert;
import com.sks.entity.AlertStatus;
import com.sks.repository.AlertRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final EmailService emailService;

    public AlertService(AlertRepository alertRepository, EmailService emailService) {
        this.alertRepository = alertRepository;
        this.emailService = emailService;
    }

    public AlertResponse createAlert(AlertRequest request) {
        Alert alert = Alert.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .recipientEmail(request.getRecipientEmail())
            .severity(request.getSeverity())
            .status(AlertStatus.CREATED)
            .createdAt(LocalDateTime.now())
            .build();

        Alert savedAlert = alertRepository.save(alert);
        sendAlertNotification(savedAlert);

        return mapToResponse(savedAlert);
    }

    public AlertResponse getAlert(Long id) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Alert not found with id: " + id));
        return mapToResponse(alert);
    }

    public List<AlertResponse> getAllAlerts() {
        return alertRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<AlertResponse> getAlertsByEmail(String email) {
        return alertRepository.findByRecipientEmail(email)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public List<AlertResponse> getAlertsByStatus(AlertStatus status) {
        return alertRepository.findByStatus(status)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public AlertResponse acknowledgeAlert(Long id, String acknowledgedBy) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Alert not found with id: " + id));

        alert.setStatus(AlertStatus.ACKNOWLEDGED);
        alert.setAcknowledgedAt(LocalDateTime.now());
        alert.setAcknowledgedBy(acknowledgedBy);

        Alert updatedAlert = alertRepository.save(alert);
        return mapToResponse(updatedAlert);
    }

    public void deleteAlert(Long id) {
        alertRepository.deleteById(id);
    }

    private void sendAlertNotification(Alert alert) {
        String subject = "Alert: " + alert.getTitle();
        String body = String.format(
            "Severity: %s\n\n%s\n\nAlert ID: %d\nCreated at: %s",
            alert.getSeverity(),
            alert.getDescription(),
            alert.getId(),
            alert.getCreatedAt()
        );

        boolean sent = emailService.sendEmail(alert.getRecipientEmail(), subject, body);
        if (sent) {
            alert.setStatus(AlertStatus.SENT);
            alert.setSentAt(LocalDateTime.now());
        } else {
            alert.setStatus(AlertStatus.FAILED);
        }
        alertRepository.save(alert);
    }

    private AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
            .id(alert.getId())
            .title(alert.getTitle())
            .description(alert.getDescription())
            .status(alert.getStatus())
            .severity(alert.getSeverity())
            .recipientEmail(alert.getRecipientEmail())
            .createdAt(alert.getCreatedAt())
            .sentAt(alert.getSentAt())
            .acknowledgedAt(alert.getAcknowledgedAt())
            .acknowledgedBy(alert.getAcknowledgedBy())
            .build();
    }
}

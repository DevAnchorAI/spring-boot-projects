package com.sks.repository;

import com.sks.entity.Alert;
import com.sks.entity.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByRecipientEmail(String recipientEmail);
    List<Alert> findByStatus(AlertStatus status);
    List<Alert> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    Optional<Alert> findByIdAndRecipientEmail(Long id, String recipientEmail);
}


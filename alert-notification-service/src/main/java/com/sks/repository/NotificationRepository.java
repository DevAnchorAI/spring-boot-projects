package com.sks.repository;

import com.sks.entity.Notification;
import com.sks.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientEmail(String recipientEmail);
    List<Notification> findByStatus(NotificationStatus status);
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);
    List<Notification> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}


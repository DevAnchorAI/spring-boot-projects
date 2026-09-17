package com.example.circuitbreaker.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * Service to demonstrate Circuit Breaker pattern using Resilience4j.
 * This service calls external APIs and handles failures gracefully.
 */
@Slf4j
@Service
public class ExternalServiceClient {

    private final RestTemplate restTemplate;

    // External service URLs (these can be configured in application.yml)
    private static final String USER_SERVICE_URL = "http://localhost:8081/api/users/{id}";
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8082/api/payments/process";
    private static final String NOTIFICATION_SERVICE_URL = "http://localhost:8083/api/notifications/send";

    public ExternalServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Calls the User Service with Circuit Breaker pattern
     * - If circuit is CLOSED: all requests pass through normally
     * - If circuit is OPEN: requests fail immediately with fallback
     * - If circuit is HALF_OPEN: a limited number of requests test if service recovered
     */
    @CircuitBreaker(
            name = "userServiceBreaker",
            fallbackMethod = "userServiceFallback"
    )
    @Retry(
            name = "userServiceRetry"
    )
    @TimeLimiter(
            name = "userServiceTimeLimiter"
    )
    public CompletableFuture<UserResponse> getUserById(Long userId) {
        log.info("Calling User Service for userId: {}", userId);
        try {
            UserResponse response = restTemplate.getForObject(
                    USER_SERVICE_URL,
                    UserResponse.class,
                    userId
            );
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Error calling User Service: {}", e.getMessage());
            throw new RuntimeException("User Service call failed", e);
        }
    }

    /**
     * Fallback method for getUserById - called when circuit is OPEN or other failures occur
     */
    public CompletableFuture<UserResponse> userServiceFallback(Long userId, Exception ex) {
        log.warn("Circuit Breaker fallback triggered for userId: {}. Error: {}", userId, ex.getMessage());
        UserResponse fallbackResponse = UserResponse.builder()
                .id(userId)
                .name("Default User")
                .email("default@example.com")
                .source("FALLBACK")
                .build();
        return CompletableFuture.completedFuture(fallbackResponse);
    }

    /**
     * Calls the Payment Service with Circuit Breaker
     */
    @CircuitBreaker(
            name = "paymentServiceBreaker",
            fallbackMethod = "paymentServiceFallback"
    )
    @Retry(
            name = "paymentServiceRetry"
    )
    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment: {}", paymentRequest);
        try {
            PaymentResponse response = restTemplate.postForObject(
                    PAYMENT_SERVICE_URL,
                    paymentRequest,
                    PaymentResponse.class
            );
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            log.error("Error processing payment: {}", e.getMessage());
            throw new RuntimeException("Payment Service call failed", e);
        }
    }

    /**
     * Fallback method for processPayment
     */
    public CompletableFuture<PaymentResponse> paymentServiceFallback(
            PaymentRequest paymentRequest,
            Exception ex) {
        log.warn("Circuit Breaker fallback triggered for payment. Error: {}", ex.getMessage());
        PaymentResponse fallbackResponse = PaymentResponse.builder()
                .transactionId("PENDING-" + System.currentTimeMillis())
                .status("PENDING")
                .message("Payment is queued for processing. Circuit Breaker is active.")
                .build();
        return CompletableFuture.completedFuture(fallbackResponse);
    }

    /**
     * Calls the Notification Service with Circuit Breaker
     */
    @CircuitBreaker(
            name = "notificationServiceBreaker",
            fallbackMethod = "notificationServiceFallback"
    )
    public NotificationResponse sendNotification(NotificationRequest notificationRequest) {
        log.info("Sending notification to: {}", notificationRequest.getRecipient());
        try {
            NotificationResponse response = restTemplate.postForObject(
                    NOTIFICATION_SERVICE_URL,
                    notificationRequest,
                    NotificationResponse.class
            );
            return response;
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
            throw new RuntimeException("Notification Service call failed", e);
        }
    }

    /**
     * Fallback method for sendNotification
     */
    public NotificationResponse notificationServiceFallback(
            NotificationRequest notificationRequest,
            Exception ex) {
        log.warn("Circuit Breaker fallback triggered for notification. Error: {}", ex.getMessage());
        NotificationResponse fallbackResponse = NotificationResponse.builder()
                .notificationId("FALLBACK-" + System.currentTimeMillis())
                .status("QUEUED")
                .message("Notification queued due to service unavailability")
                .build();
        return fallbackResponse;
    }
}

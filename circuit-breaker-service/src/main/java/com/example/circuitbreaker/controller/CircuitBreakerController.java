package com.example.circuitbreaker.controller;

import com.example.circuitbreaker.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller demonstrating Circuit Breaker pattern
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class CircuitBreakerController {

    private final ExternalServiceClient externalServiceClient;

    public CircuitBreakerController(ExternalServiceClient externalServiceClient) {
        this.externalServiceClient = externalServiceClient;
    }

    /**
     * Get user by ID using Circuit Breaker
     *
     * @param userId User ID
     * @return User information or fallback data
     */
    @GetMapping("/users/{userId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getUser(@PathVariable Long userId) {
        log.info("Received request for userId: {}", userId);

        return externalServiceClient.getUserById(userId)
                .thenApply(userResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", userResponse);
                    return ResponseEntity.ok(response);
                })
                .exceptionally(ex -> {
                    log.error("Error getting user: {}", ex.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("error", ex.getMessage());
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
                });
    }

    /**
     * Process payment using Circuit Breaker
     *
     * @param paymentRequest Payment details
     * @return Payment processing result
     */
    @PostMapping("/payments/process")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> processPayment(
            @RequestBody PaymentRequest paymentRequest) {
        log.info("Received payment request: {}", paymentRequest);

        return externalServiceClient.processPayment(paymentRequest)
                .thenApply(paymentResponse -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", paymentResponse);
                    return ResponseEntity.ok(response);
                })
                .exceptionally(ex -> {
                    log.error("Error processing payment: {}", ex.getMessage());
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("error", ex.getMessage());
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
                });
    }

    /**
     * Send notification using Circuit Breaker
     *
     * @param notificationRequest Notification details
     * @return Notification result
     */
    @PostMapping("/notifications/send")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @RequestBody NotificationRequest notificationRequest) {
        log.info("Received notification request for: {}", notificationRequest.getRecipient());

        try {
            NotificationResponse response = externalServiceClient.sendNotification(notificationRequest);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            log.error("Error sending notification: {}", ex.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Circuit Breaker Service");
        return ResponseEntity.ok(response);
    }
}

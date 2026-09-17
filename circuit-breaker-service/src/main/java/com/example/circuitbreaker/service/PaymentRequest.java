package com.example.circuitbreaker.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payment request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String transactionId;
    private Double amount;
    private String currency;
    private String method;
    private String description;
}

package com.spring.ai.dto;

import java.math.BigDecimal;

public record TransactionStatusData(
        String transactionId,
        String status,
        BigDecimal amount,
        String transactionType
) {
}
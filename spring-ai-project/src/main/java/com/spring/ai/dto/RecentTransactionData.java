package com.spring.ai.dto;

import java.math.BigDecimal;

public record RecentTransactionData(
        String transactionId,
        String transactionType,
        BigDecimal amount,
        String status
) {
}
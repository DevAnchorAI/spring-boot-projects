package com.spring.ai.dto;

import java.math.BigDecimal;

public record AccountBalanceData(
        String accountNumber,
        BigDecimal availableBalance,
        String currency,
        String accountStatus
) {
}
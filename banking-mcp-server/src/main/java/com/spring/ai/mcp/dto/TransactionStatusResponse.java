package com.spring.ai.mcp.dto;

public record TransactionStatusResponse(
        String transactionId,
        String status,
        Double amount
) {
}